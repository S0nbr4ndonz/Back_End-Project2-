package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateApplicationNoteRequest;
import com.group7.jobTrackerApplication.DTO.UpdateApplicationNoteRequest;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.ApplicationNote;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.ApplicationNoteRepository;
import com.group7.jobTrackerApplication.service.ApplicationNotesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationNotesServiceTests {

    @Mock private ApplicationNoteRepository applicationNoteRepository;

    private ApplicationNotesService applicationNotesService;

    @BeforeEach
    void setUp() {
        applicationNotesService = new ApplicationNotesService(applicationNoteRepository);
    }

    // getNoteById -> can throw -> 2 tests
    @Test
    void getNoteById_whenFound_returnsNote() {
        User user = new User();
        user.setUserId(1L);

        ApplicationNote note = new ApplicationNote();

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(10L, 20L, 1L))
                .thenReturn(Optional.of(note));

        ApplicationNote result = applicationNotesService.getNoteById(10L, 20L, user);

        assertSame(note, result);
        verify(applicationNoteRepository)
                .findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(10L, 20L, 1L);
        verifyNoMoreInteractions(applicationNoteRepository);
    }

    @Test
    void getNoteById_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(1L);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(10L, 20L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> applicationNotesService.getNoteById(10L, 20L, user));

        verify(applicationNoteRepository)
                .findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(10L, 20L, 1L);
        verifyNoMoreInteractions(applicationNoteRepository);
    }

    // create -> no exception -> 1 test
    @Test
    void create_savesNewNote() {
        User user = new User();
        user.setUserId(1L);

        CreateApplicationNoteRequest request = mock(CreateApplicationNoteRequest.class);
        when(request.content()).thenReturn("hello");
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0);
        when(request.lastEdited()).thenReturn(now);

        ApplicationNote saved = new ApplicationNote();
        when(applicationNoteRepository.save(any(ApplicationNote.class))).thenReturn(saved);

        ApplicationNote result = applicationNotesService.create(request, user);

        assertSame(saved, result);

        ArgumentCaptor<ApplicationNote> captor = ArgumentCaptor.forClass(ApplicationNote.class);
        verify(applicationNoteRepository).save(captor.capture());

        ApplicationNote toSave = captor.getValue();
        assertEquals("hello", toSave.getContent());
        assertEquals(now, toSave.getLastEdited());

        verifyNoMoreInteractions(applicationNoteRepository);
    }

    // patch -> can throw ForbiddenException -> 2 tests
    @Test
    void patch_whenOwned_updatesOnlyNonNullFields() {
        User user = new User();
        user.setUserId(2L);

        ApplicationNote existing = new ApplicationNote();
        existing.setContent("old");
        existing.setLastEdited(LocalDateTime.of(2026, 1, 1, 0, 0));

        UpdateApplicationNoteRequest request = mock(UpdateApplicationNoteRequest.class);

        // request.application().getApplicationId() is used in your repo call
        var application = mock(com.group7.jobTrackerApplication.model.JobApplication.class);
        when(application.getApplicationId()).thenReturn(99L);
        when(request.application()).thenReturn(application);

        when(request.content()).thenReturn("new");
        when(request.lastEdited()).thenReturn(null);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(5L, 99L, 2L))
                .thenReturn(Optional.of(existing));
        when(applicationNoteRepository.save(any(ApplicationNote.class))).thenAnswer(inv -> inv.getArgument(0));

        ApplicationNote result = applicationNotesService.patch(5L, request, user);

        assertEquals("new", result.getContent()); // updated
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), result.getLastEdited()); // unchanged

        verify(applicationNoteRepository)
                .findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(5L, 99L, 2L);
        verify(applicationNoteRepository).save(existing);
        verifyNoMoreInteractions(applicationNoteRepository);
    }

    @Test
    void patch_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(2L);

        UpdateApplicationNoteRequest request = mock(UpdateApplicationNoteRequest.class);
        var application = mock(com.group7.jobTrackerApplication.model.JobApplication.class);
        when(application.getApplicationId()).thenReturn(99L);
        when(request.application()).thenReturn(application);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(5L, 99L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> applicationNotesService.patch(5L, request, user));

        verify(applicationNoteRepository)
                .findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(5L, 99L, 2L);
        verifyNoMoreInteractions(applicationNoteRepository);
    }

    // delete -> can throw ForbiddenException -> 2 tests
    @Test
    void delete_whenOwned_deletesById() {
        User user = new User();
        user.setUserId(3L);

        ApplicationNote existing = new ApplicationNote();
        existing.setNotesId(77L);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(77L, 55L, 3L))
                .thenReturn(Optional.of(existing));

        applicationNotesService.delete(77L, 55L, user);

        verify(applicationNoteRepository)
                .findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(77L, 55L, 3L);
        verify(applicationNoteRepository).deleteById(77L);
        verifyNoMoreInteractions(applicationNoteRepository);
    }

    @Test
    void delete_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(3L);

        when(applicationNoteRepository.findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(77L, 55L, 3L))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> applicationNotesService.delete(77L, 55L, user));

        verify(applicationNoteRepository)
                .findByNotesIdAndApplication_ApplicationIdAndApplication_User_UserId(77L, 55L, 3L);
        verifyNoMoreInteractions(applicationNoteRepository);
    }
}