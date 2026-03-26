package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateApplicationNoteRequest;
import com.group7.jobTrackerApplication.DTO.UpdateApplicationNoteRequest;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.ApplicationNote;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.ApplicationNoteRepository;
import com.group7.jobTrackerApplication.repository.JobApplicationRepository;
import com.group7.jobTrackerApplication.service.ApplicationNotesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationNotesServiceTests {

    @Mock
    private ApplicationNoteRepository applicationNoteRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    private ApplicationNotesService applicationNotesService;

    @BeforeEach
    void setUp() {
        applicationNotesService = new ApplicationNotesService(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void getNoteById_whenFound_returnsNote() {
        User user = new User();
        user.setUserId(1L);
        ApplicationNote note = new ApplicationNote();

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(10L, 20L, 1L))
                .thenReturn(Optional.of(note));

        ApplicationNote result = applicationNotesService.getNoteById(10L, 20L, user);

        assertSame(note, result);
        verify(applicationNoteRepository).findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(10L, 20L, 1L);
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void getNoteById_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(1L);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(10L, 20L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> applicationNotesService.getNoteById(10L, 20L, user));

        verify(applicationNoteRepository).findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(10L, 20L, 1L);
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void create_whenNoExistingNote_createsAndSavesNewNote() {
        User user = new User();
        user.setUserId(1L);

        JobApplication jobApplication = new JobApplication();
        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(20L, 1L)).thenReturn(Optional.of(jobApplication));
        when(applicationNoteRepository.findByApplication_ApplicationIdAndApplication_User_UserId(20L, 1L)).thenReturn(Optional.empty());
        when(applicationNoteRepository.save(any(ApplicationNote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime edited = LocalDateTime.of(2026, 1, 1, 12, 0);
        ApplicationNote result = applicationNotesService.create(
                20L,
                new CreateApplicationNoteRequest("hello", edited, jobApplication),
                user
        );

        assertEquals("hello", result.getContent());
        assertEquals(edited, result.getLastEdited());
        assertSame(jobApplication, result.getApplication());
        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(20L, 1L);
        verify(applicationNoteRepository).findByApplication_ApplicationIdAndApplication_User_UserId(20L, 1L);
        verify(applicationNoteRepository).save(any(ApplicationNote.class));
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void create_whenExistingNotePresent_updatesExistingNote() {
        User user = new User();
        user.setUserId(1L);

        JobApplication jobApplication = new JobApplication();
        ApplicationNote existing = new ApplicationNote();
        existing.setContent("old");
        existing.setLastEdited(LocalDateTime.of(2026, 1, 1, 10, 0));

        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(20L, 1L)).thenReturn(Optional.of(jobApplication));
        when(applicationNoteRepository.findByApplication_ApplicationIdAndApplication_User_UserId(20L, 1L))
                .thenReturn(Optional.of(existing));
        when(applicationNoteRepository.save(existing)).thenReturn(existing);

        LocalDateTime edited = LocalDateTime.of(2026, 1, 1, 12, 0);
        ApplicationNote result = applicationNotesService.create(
                20L,
                new CreateApplicationNoteRequest("new content", edited, jobApplication),
                user
        );

        assertSame(existing, result);
        assertEquals("new content", existing.getContent());
        assertEquals(edited, existing.getLastEdited());
        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(20L, 1L);
        verify(applicationNoteRepository).findByApplication_ApplicationIdAndApplication_User_UserId(20L, 1L);
        verify(applicationNoteRepository).save(existing);
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void create_whenApplicationNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(1L);
        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(20L, 1L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> applicationNotesService.create(20L, new CreateApplicationNoteRequest("hello", LocalDateTime.now(), new JobApplication()), user));

        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(20L, 1L);
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void patch_whenOwned_updatesContentOnly() {
        User user = new User();
        user.setUserId(2L);

        JobApplication application = new JobApplication();
        application.setApplicationId(99L);

        ApplicationNote existing = new ApplicationNote();
        existing.setContent("old");
        existing.setLastEdited(LocalDateTime.of(2026, 1, 1, 0, 0));

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(5L, 99L, 2L))
                .thenReturn(Optional.of(existing));
        when(applicationNoteRepository.save(existing)).thenReturn(existing);

        ApplicationNote result = applicationNotesService.patch(
                99L,
                5L,
                new UpdateApplicationNoteRequest("new", application),
                user
        );

        assertEquals("new", result.getContent());
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.getLastEdited());
        verify(applicationNoteRepository).findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(5L, 99L, 2L);
        verify(applicationNoteRepository).save(existing);
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void patch_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(2L);

        JobApplication application = new JobApplication();
        application.setApplicationId(99L);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(5L, 99L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> applicationNotesService.patch(99L, 5L, new UpdateApplicationNoteRequest("new", application), user));

        verify(applicationNoteRepository).findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(5L, 99L, 2L);
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void delete_whenOwned_deletesById() {
        User user = new User();
        user.setUserId(3L);

        ApplicationNote existing = new ApplicationNote();
        existing.setNotesId(77L);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(77L, 55L, 3L))
                .thenReturn(Optional.of(existing));

        applicationNotesService.delete(77L, 55L, user);

        verify(applicationNoteRepository).findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(77L, 55L, 3L);
        verify(applicationNoteRepository).deleteById(77L);
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }

    @Test
    void delete_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(3L);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(77L, 55L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> applicationNotesService.delete(77L, 55L, user));

        verify(applicationNoteRepository).findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(77L, 55L, 3L);
        verifyNoMoreInteractions(applicationNoteRepository, jobApplicationRepository);
    }
}
