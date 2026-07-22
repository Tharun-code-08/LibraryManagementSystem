package com.university.lms.business;

import java.util.Optional;

import com.university.lms.entity.Faculty;
import com.university.lms.entity.HolderType;
import com.university.lms.entity.Student;
import com.university.lms.entity.User;
import com.university.lms.repository.FacultyRepository;
import com.university.lms.repository.StudentRepository;

/**
 * Resolves the barcode-scanned or typed member identifier used at circulation desks (student ID,
 * roll number, or faculty ID) to a {@link HolderRef}, without either circulation service needing
 * to know how student/faculty identity lookup works.
 */
public final class MembershipHolderResolver {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public MembershipHolderResolver(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    public Optional<HolderRef> resolveByIdentifier(String memberIdentifier) {
        Optional<Student> student = studentRepository.findByStudentId(memberIdentifier)
                .or(() -> studentRepository.findByRollNumber(memberIdentifier));
        if (student.isPresent()) {
            Student s = student.get();
            return Optional.of(new HolderRef(HolderType.STUDENT, s.getId(), s.getUser().getUsername()));
        }

        Optional<Faculty> faculty = facultyRepository.findByFacultyId(memberIdentifier);
        if (faculty.isPresent()) {
            Faculty f = faculty.get();
            return Optional.of(new HolderRef(HolderType.FACULTY, f.getId(), f.getUser().getUsername()));
        }

        return Optional.empty();
    }

    public String resolveDisplayName(HolderType holderType, Long holderId) {
        if (holderType == HolderType.STUDENT) {
            return studentRepository.findById(holderId).map(s -> s.getUser().getUsername()).orElse("Unknown");
        }
        return facultyRepository.findById(holderId).map(f -> f.getUser().getUsername()).orElse("Unknown");
    }

    /** Resolves the linked {@link User} account of a membership holder, e.g. to send a notification. */
    public Optional<User> resolveUser(HolderType holderType, Long holderId) {
        if (holderType == HolderType.STUDENT) {
            return studentRepository.findById(holderId).map(Student::getUser);
        }
        return facultyRepository.findById(holderId).map(Faculty::getUser);
    }
}
