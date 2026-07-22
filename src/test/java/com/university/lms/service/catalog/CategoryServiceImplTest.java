package com.university.lms.service.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.dto.request.CategoryRequestDTO;
import com.university.lms.dto.response.CategoryDTO;
import com.university.lms.entity.Category;
import com.university.lms.exception.DuplicateResourceException;
import com.university.lms.exception.ResourceNotFoundException;
import com.university.lms.repository.CategoryRepository;
import com.university.lms.security.AuthContext;
import com.university.lms.service.auth.AuditLogService;
import com.university.lms.service.catalog.impl.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuditLogService auditLogService;

    private CategoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CategoryServiceImpl(categoryRepository, auditLogService, new AuthContext());
        lenient().when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsTopLevelCategory() {
        when(categoryRepository.findByNameAndParent("Fiction", null)).thenReturn(Optional.empty());

        CategoryDTO result = service.save(new CategoryRequestDTO(null, "Fiction", "desc", null));

        assertEquals("Fiction", result.name());
    }

    @Test
    void rejectsDuplicateNameAtSameLevel() {
        when(categoryRepository.findByNameAndParent("Fiction", null))
                .thenReturn(Optional.of(new Category("Fiction", null, null)));

        assertThrows(DuplicateResourceException.class,
                () -> service.save(new CategoryRequestDTO(null, "Fiction", null, null)));
    }

    @Test
    void createThrowsWhenParentMissing() {
        when(categoryRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.save(new CategoryRequestDTO(null, "Sci-Fi", null, 9L)));
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> service.save(new CategoryRequestDTO(null, "", null, null)));
    }

    @Test
    void deletesLeafCategory() {
        Category category = new Category("Fiction", null, null);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        service.delete(5L);

        org.mockito.Mockito.verify(categoryRepository).delete(category);
    }

    @Test
    void deleteThrowsWhenCategoryHasChildren() {
        Category parent = new Category("Fiction", null, null);
        setId(parent, 5L);
        Category child = new Category("Sci-Fi", parent, null);
        setId(child, 6L);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(parent));
        when(categoryRepository.findAll()).thenReturn(List.of(parent, child));

        assertThrows(IllegalStateException.class, () -> service.delete(5L));
    }

    @Test
    void listTreeBuildsNestedStructure() {
        Category parent = new Category("Fiction", null, null);
        setId(parent, 5L);
        Category child = new Category("Sci-Fi", parent, null);
        setId(child, 6L);
        when(categoryRepository.findAll()).thenReturn(List.of(parent, child));

        List<CategoryDTO> tree = service.listTree();

        assertEquals(1, tree.size());
        assertTrue(tree.get(0).children().stream().anyMatch(c -> c.name().equals("Sci-Fi")));
    }

    private static void setId(Category category, Long id) {
        try {
            java.lang.reflect.Field field = Category.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(category, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
