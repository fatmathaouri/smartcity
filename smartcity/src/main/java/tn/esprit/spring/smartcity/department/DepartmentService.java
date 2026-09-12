package tn.esprit.spring.smartcity.department;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.smartcity.agent.MunicipalAgentRepository;
import tn.esprit.spring.smartcity.category.Category;
import tn.esprit.spring.smartcity.category.CategoryRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final MunicipalAgentRepository agentRepository;

    @Transactional
    public Department createDepartment(String name, String description, Long categoryId) {
        if (departmentRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Department already exists: " + name);
        }
        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }
        return departmentRepository.save(Department.builder()
                .name(name)
                .description(description)
                .category(category)
                .isActive(true)
                .build());
    }

    @Transactional
    public Department updateDepartment(Long id, String name, String description, Long categoryId) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        if (name != null) dept.setName(name);
        if (description != null) dept.setDescription(description);
        if (categoryId != null) {
            Category cat = categoryRepository.findById(categoryId).orElse(null);
            dept.setCategory(cat);
        }
        return departmentRepository.save(dept);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        dept.setIsActive(false);
        departmentRepository.save(dept);
    }

    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findByIsActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DepartmentDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        return toDto(dept);
    }

    public Map<String, Object> getDepartmentStats(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found"));
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("department", toDto(dept));
        long agentCount = agentRepository.countByDepartmentId(id);
        stats.put("agentCount", agentCount);
        return stats;
    }

    public DepartmentDto toDto(Department dept) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setDescription(dept.getDescription());
        dto.setIsActive(dept.getIsActive());
        if (dept.getCategory() != null) {
            dto.setCategoryId(dept.getCategory().getId());
            dto.setCategoryName(dept.getCategory().getName());
        }
        long agentCount = agentRepository.countByDepartmentId(dept.getId());
        dto.setAgentCount((int) agentCount);
        return dto;
    }
}
