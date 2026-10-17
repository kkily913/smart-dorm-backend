package com.youlai.boot.system.controller;

import com.youlai.boot.framework.security.model.SecurityUserDetails;
import com.youlai.boot.framework.security.port.PermissionPort;
import com.youlai.boot.framework.security.service.PermissionService;
import com.youlai.boot.system.model.entity.BizBuilding;
import com.youlai.boot.system.model.entity.BizRoom;
import com.youlai.boot.system.model.entity.BizStudent;
import com.youlai.boot.system.service.IBizBuildingService;
import com.youlai.boot.system.service.IBizRoomService;
import com.youlai.boot.system.service.IBizStudentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real Spring method-security proxies and PermissionService; no database/Redis or dev profile. */
@SpringJUnitConfig(BizControllerPermissionTest.Config.class)
class BizControllerPermissionTest {
    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity
    @Import({BizBuildingController.class, BizRoomController.class, BizStudentController.class,
            PermissionService.class})
    static class Config {
        @Bean PermissionPort permissionPort() { return mock(PermissionPort.class); }
        @Bean IBizBuildingService buildingService() { return mock(IBizBuildingService.class); }
        @Bean IBizRoomService roomService() { return mock(IBizRoomService.class); }
        @Bean IBizStudentService studentService() { return mock(IBizStudentService.class); }
    }

    @Autowired ApplicationContext context;
    @Autowired PermissionPort permissions;
    @Autowired IBizBuildingService buildings;
    @Autowired IBizRoomService rooms;
    @Autowired IBizStudentService students;

    record Endpoint(String permission, Function<ApplicationContext, Object> call) {
        @Override public String toString() { return permission; }
    }

    static Stream<Endpoint> endpoints() {
        return Stream.of(
                new Endpoint("biz:building:list", c -> c.getBean(BizBuildingController.class).getPage(1, 10, null)),
                new Endpoint("biz:building:create", c -> c.getBean(BizBuildingController.class).add(new BizBuilding())),
                new Endpoint("biz:building:update", c -> c.getBean(BizBuildingController.class).update(1L, new BizBuilding())),
                new Endpoint("biz:building:delete", c -> c.getBean(BizBuildingController.class).delete(new Long[]{1L})),
                new Endpoint("biz:room:list", c -> c.getBean(BizRoomController.class).getRoomPage(1, 10, null, null)),
                new Endpoint("biz:room:create", c -> c.getBean(BizRoomController.class).addRoom(new BizRoom())),
                new Endpoint("biz:room:update", c -> c.getBean(BizRoomController.class).updateRoom(1L, new BizRoom())),
                new Endpoint("biz:room:delete", c -> c.getBean(BizRoomController.class).deleteRoom(new Long[]{1L})),
                new Endpoint("biz:student:list", c -> c.getBean(BizStudentController.class).getStudentPage(1, 10, null, null)),
                new Endpoint("biz:student:create", c -> c.getBean(BizStudentController.class).addStudent(new BizStudent())),
                new Endpoint("biz:student:update", c -> c.getBean(BizStudentController.class).updateStudent(1L, new BizStudent())),
                new Endpoint("biz:student:delete", c -> c.getBean(BizStudentController.class).deleteStudent(1L))
        );
    }

    @BeforeEach void resetState() {
        SecurityContextHolder.clearContext();
        reset(permissions, buildings, rooms, students);
        when(permissions.getRolePerms(anySet())).thenReturn(Set.of());
    }

    @AfterEach void clearIdentity() { SecurityContextHolder.clearContext(); }

    private void login(String role) {
        SecurityUserDetails user = new SecurityUserDetails();
        user.setUserId(99L);
        user.setUsername("permission-test");
        user.setRoles(Set.of(role));
        user.setEnabled(true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private void assertBlocked(Endpoint endpoint) {
        assertThrows(AccessDeniedException.class, () -> endpoint.call.apply(context));
        // Denied requests must not reach reads or mutations in any business service.
        verifyNoInteractions(buildings, rooms, students);
    }

    private void assertAllowed(Endpoint endpoint) {
        assertDoesNotThrow(() -> endpoint.call.apply(context));
        assertEquals(1, mockingDetails(buildings).getInvocations().size()
                + mockingDetails(rooms).getInvocations().size()
                + mockingDetails(students).getInvocations().size());
    }

    @ParameterizedTest @MethodSource("endpoints")
    void authenticatedWithoutPermissionCannotAccess(Endpoint endpoint) {
        login("TEST");
        assertBlocked(endpoint);
    }

    @ParameterizedTest @MethodSource("endpoints")
    void exactPermissionCanAccess(Endpoint endpoint) {
        login("DORM_OPERATOR");
        when(permissions.getRolePerms(Set.of("DORM_OPERATOR"))).thenReturn(Set.of(endpoint.permission));
        assertAllowed(endpoint);
    }

    @ParameterizedTest @MethodSource("endpoints")
    void otherModulePermissionCannotAccess(Endpoint endpoint) {
        login("TEST");
        String otherModule = endpoint.permission.startsWith("biz:student:") ? "biz:room:*" : "biz:student:*";
        when(permissions.getRolePerms(anySet())).thenReturn(Set.of(otherModule));
        assertBlocked(endpoint);
    }

    @ParameterizedTest @MethodSource("endpoints")
    void otherActionPermissionCannotAccess(Endpoint endpoint) {
        login("TEST");
        String prefix = endpoint.permission.substring(0, endpoint.permission.lastIndexOf(':') + 1);
        String otherAction = endpoint.permission.endsWith(":list") ? "create" : "list";
        when(permissions.getRolePerms(anySet())).thenReturn(Set.of(prefix + otherAction));
        assertBlocked(endpoint);
    }

    @ParameterizedTest @MethodSource("endpoints")
    void rootRetainsExistingAccess(Endpoint endpoint) {
        login("ROOT");
        assertAllowed(endpoint);
        verifyNoInteractions(permissions);
    }

    @ParameterizedTest @MethodSource("endpoints")
    void anonymousCannotAccess(Endpoint endpoint) {
        assertBlocked(endpoint);
    }
}
