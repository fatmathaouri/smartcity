package tn.esprit.spring.smartcity.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.smartcity.report.Priority;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setDefaultPriority(category.getDefaultPriority() != null ? category.getDefaultPriority().name() : null);
        dto.setSlaHours(category.getSlaHours());
        dto.setEscalationHours(category.getEscalationHours());
        return dto;
    }

    public List<CategoryDto> toDtoList(List<Category> categories) {
        return categories.stream().map(this::toDto).toList();
    }

    public void seedDefaultCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(Category.builder().name("Voirie").description("Problèmes de routes, trottoirs, nids-de-poule").defaultPriority(Priority.HIGH).slaHours(72).escalationHours(48).build());
            categoryRepository.save(Category.builder().name("Propreté").description("Déchets, poubelles, nettoyage").defaultPriority(Priority.MEDIUM).slaHours(168).escalationHours(72).build());
            categoryRepository.save(Category.builder().name("Éclairage").description("Lampadaires, éclairage public").defaultPriority(Priority.MEDIUM).slaHours(48).escalationHours(24).build());
            categoryRepository.save(Category.builder().name("Espaces Verts").description("Parcs, jardins, arbres").defaultPriority(Priority.LOW).slaHours(168).escalationHours(72).build());
            categoryRepository.save(Category.builder().name("Eau et Assainissement").description("Fuite d'eau, égouts, inondation").defaultPriority(Priority.HIGH).slaHours(24).escalationHours(12).build());
            categoryRepository.save(Category.builder().name("Stationnement").description("Problèmes de parking, stationnement").defaultPriority(Priority.LOW).slaHours(120).escalationHours(48).build());
        }
    }
}
