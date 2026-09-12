package tn.esprit.spring.smartcity.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.smartcity.auth.CreateUserRequest;
import tn.esprit.spring.smartcity.auth.UserCreationResult;
import tn.esprit.spring.smartcity.category.Category;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<UserCreationResult> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(adminService.createUser(request));
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<UserCreationResult> resetTempPassword(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.resetTempPassword(id));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id, @RequestParam String role) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }

    @DeleteMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> removeUserRole(
            @PathVariable Long id, @RequestParam String role) {
        return ResponseEntity.ok(adminService.removeUserRole(id, role));
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUserEnabled(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserEnabled(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/role-assignments")
    public ResponseEntity<List<Map<String, Object>>> getRoleAssignments() {
        return ResponseEntity.ok(adminService.getRoleAssignments());
    }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.createCategory(
                body.get("name"), body.get("description"), body.get("priority")));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.updateCategory(
                id, body.get("name"), body.get("description"), body.get("priority")));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/departments-manager-status")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentsManagerStatus() {
        return ResponseEntity.ok(adminService.getDepartmentsManagerStatus());
    }

    @PutMapping("/managers/{userId}/department")
    public ResponseEntity<Map<String, Object>> reassignManager(
            @PathVariable Long userId, @RequestParam Long departmentId) {
        return ResponseEntity.ok(adminService.reassignManagerDepartment(userId, departmentId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getGlobalStats() {
        return ResponseEntity.ok(adminService.getGlobalStats());
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<Map<String, Object>>> getHeatmapData() {
        return ResponseEntity.ok(adminService.getHeatmapData());
    }

    @GetMapping("/reports-by-month")
    public ResponseEntity<List<Map<String, Object>>> getReportsByMonth() {
        return ResponseEntity.ok(adminService.getReportsByMonth());
    }
}
