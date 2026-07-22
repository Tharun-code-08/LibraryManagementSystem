package com.university.lms.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.university.lms.entity.Faculty;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Student;
import com.university.lms.entity.User;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
class MembershipHolderResolverTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    private MembershipHolderResolver resolver;
    private Student student;
    private Faculty faculty;

    @BeforeEach
    void setUp() {
        resolver = new MembershipHolderResolver(studentRepository, facultyRepository);

        User studentUser = new User("jdoe", "jdoe@university.edu", "hash", null);
        student = new Student(studentUser, "STU1001", "R1001", null);
        setId(student, 1L);

        User facultyUser = new User("profx", "profx@university.edu", "hash", null);
        faculty = new Faculty(facultyUser, "FAC1001");
        setId(faculty, 2L);

        lenient().when(studentRepository.findByStudentId(any())).thenReturn(Optional.empty());
        lenient().when(studentRepository.findByRollNumber(any())).thenReturn(Optional.empty());
        lenient().when(facultyRepository.findByFacultyId(any())).thenReturn(Optional.empty());
    }

    @Test
    void resolvesByStudentId() {
        when(studentRepository.findByStudentId("STU1001")).thenReturn(Optional.of(student));

        Optional<HolderRef> result = resolver.resolveByIdentifier("STU1001");

        assertTrue(result.isPresent());
        assertEquals(HolderType.STUDENT, result.get().holderType());
        assertEquals(1L, result.get().holderId());
        assertEquals("jdoe", result.get().displayName());
    }

    @Test
    void resolvesByRollNumberWhenStudentIdLookupMisses() {
        when(studentRepository.findByRollNumber("R1001")).thenReturn(Optional.of(student));

        Optional<HolderRef> result = resolver.resolveByIdentifier("R1001");

        assertTrue(result.isPresent());
        assertEquals(HolderType.STUDENT, result.get().holderType());
    }

    @Test
    void fallsBackToFacultyWhenNoStudentMatches() {
        when(facultyRepository.findByFacultyId("FAC1001")).thenReturn(Optional.of(faculty));

        Optional<HolderRef> result = resolver.resolveByIdentifier("FAC1001");

        assertTrue(result.isPresent());
        assertEquals(HolderType.FACULTY, result.get().holderType());
        assertEquals(2L, result.get().holderId());
        assertEquals("profx", result.get().displayName());
    }

    @Test
    void returnsEmptyWhenIdentifierMatchesNothing() {
        Optional<HolderRef> result = resolver.resolveByIdentifier("UNKNOWN");

        assertFalse(result.isPresent());
    }

    @Test
    void resolveDisplayNameReturnsStudentUsername() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertEquals("jdoe", resolver.resolveDisplayName(HolderType.STUDENT, 1L));
    }

    @Test
    void resolveDisplayNameReturnsFacultyUsername() {
        when(facultyRepository.findById(2L)).thenReturn(Optional.of(faculty));

        assertEquals("profx", resolver.resolveDisplayName(HolderType.FACULTY, 2L));
    }

    @Test
    void resolveDisplayNameReturnsUnknownWhenHolderMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertEquals("Unknown", resolver.resolveDisplayName(HolderType.STUDENT, 99L));
    }

    @Test
    void resolveUserReturnsStudentUser() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Optional<User> result = resolver.resolveUser(HolderType.STUDENT, 1L);

        assertTrue(result.isPresent());
        assertEquals("jdoe", result.get().getUsername());
    }

    @Test
    void resolveUserReturnsFacultyUser() {
        when(facultyRepository.findById(2L)).thenReturn(Optional.of(faculty));

        Optional<User> result = resolver.resolveUser(HolderType.FACULTY, 2L);

        assertTrue(result.isPresent());
        assertEquals("profx", result.get().getUsername());
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
