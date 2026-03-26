package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.GetJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobApplicationRequest;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.ApplicationNote;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.model.JobEntry;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobApplicationRepository;
import com.group7.jobTrackerApplication.repository.JobEntryRepository;
import com.group7.jobTrackerApplication.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTests {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobEntryRepository jobEntryRepository;

    private JobApplicationService jobApplicationService;

    @BeforeEach
    void setUp() {
        jobApplicationService = new JobApplicationService(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void getAll_whenFound_returnsApplicationDtos() {
        User user = new User();
        user.setUserId(1L);

        JobEntry jobEntry = new JobEntry();
        jobEntry.setJobId(11L);
        jobEntry.setJobTitle("Backend Engineer");

        ApplicationNote note = new ApplicationNote();
        note.setNotesId(55L);

        JobApplication application = new JobApplication();
        application.setApplicationId(99L);
        application.setJobEntry(jobEntry);
        application.setStatus("APPLIED");
        application.setDateApplied(LocalDate.of(2026, 3, 1));
        application.setNote(note);

        when(jobApplicationRepository.findAllByUser_UserId(1L)).thenReturn(Optional.of(List.of(application)));

        List<GetJobApplicationRequest> result = jobApplicationService.getAll(user);

        assertEquals(1, result.size());
        assertEquals(99L, result.get(0).applicationId());
        assertEquals(11L, result.get(0).jobId());
        assertEquals("Backend Engineer", result.get(0).jobTitle());
        assertEquals(55L, result.get(0).notesId());
        verify(jobApplicationRepository).findAllByUser_UserId(1L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void getAll_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(1L);
        when(jobApplicationRepository.findAllByUser_UserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobApplicationService.getAll(user));

        verify(jobApplicationRepository).findAllByUser_UserId(1L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void getById_whenFound_returnsApplicationDto() {
        User user = new User();
        user.setUserId(2L);

        JobEntry jobEntry = new JobEntry();
        jobEntry.setJobId(10L);
        jobEntry.setJobTitle("SWE Intern");

        JobApplication app = new JobApplication();
        app.setApplicationId(77L);
        app.setJobEntry(jobEntry);
        app.setStatus("INTERVIEW");
        app.setDateApplied(LocalDate.of(2026, 2, 4));

        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(77L, 2L)).thenReturn(Optional.of(app));

        GetJobApplicationRequest result = jobApplicationService.getById(77L, user);

        assertEquals(77L, result.applicationId());
        assertEquals("SWE Intern", result.jobTitle());
        assertEquals("INTERVIEW", result.status());
        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(77L, 2L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void getById_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(2L);
        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobApplicationService.getById(10L, user));

        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(10L, 2L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void create_whenOwnedJobEntryExists_savesNewApplication() {
        User user = new User();
        user.setUserId(3L);

        JobEntry jobEntry = new JobEntry();
        jobEntry.setJobId(99L);
        when(jobEntryRepository.findByJobIdAndUser_UserId(99L, 3L)).thenReturn(Optional.of(jobEntry));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobApplication result = jobApplicationService.create(
                new CreateJobApplicationRequest(99L, "Applied", LocalDate.of(2026, 1, 1)),
                user
        );

        assertEquals("Applied", result.getStatus());
        assertEquals(LocalDate.of(2026, 1, 1), result.getDateApplied());
        assertEquals(jobEntry, result.getJobEntry());
        assertEquals(user, result.getUser());
        verify(jobEntryRepository).findByJobIdAndUser_UserId(99L, 3L);
        verify(jobApplicationRepository).save(any(JobApplication.class));
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void create_whenOwnedJobEntryMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(3L);
        when(jobEntryRepository.findByJobIdAndUser_UserId(99L, 3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobApplicationService.create(new CreateJobApplicationRequest(99L, "Applied", LocalDate.now()), user));

        verify(jobEntryRepository).findByJobIdAndUser_UserId(99L, 3L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void replace_whenOwned_updatesAndSaves() {
        User user = new User();
        user.setUserId(4L);

        JobApplication existing = new JobApplication();
        JobEntry replacementEntry = new JobEntry();
        replacementEntry.setJobId(123L);

        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(20L, 4L)).thenReturn(Optional.of(existing));
        when(jobEntryRepository.findByJobIdAndUser_UserId(123L, 4L)).thenReturn(Optional.of(replacementEntry));
        when(jobApplicationRepository.save(existing)).thenReturn(existing);

        JobApplication result = jobApplicationService.replace(
                20L,
                new UpdateJobApplicationRequest(123L, "Interview", LocalDate.of(2026, 2, 2)),
                user
        );

        assertEquals(LocalDate.of(2026, 2, 2), result.getDateApplied());
        assertEquals("Interview", result.getStatus());
        assertEquals(123L, result.getJobEntry().getJobId());
        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(20L, 4L);
        verify(jobEntryRepository).findByJobIdAndUser_UserId(123L, 4L);
        verify(jobApplicationRepository).save(existing);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void replace_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(4L);
        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(20L, 4L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> jobApplicationService.replace(20L, new UpdateJobApplicationRequest(123L, "Interview", LocalDate.now()), user));

        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(20L, 4L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void patch_whenOwned_updatesOnlyNonNullFields() {
        User user = new User();
        user.setUserId(5L);

        JobEntry originalEntry = new JobEntry();
        originalEntry.setJobId(1L);

        JobApplication existing = new JobApplication();
        existing.setStatus("Applied");
        existing.setDateApplied(LocalDate.of(2026, 1, 1));
        existing.setJobEntry(originalEntry);

        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(30L, 5L)).thenReturn(Optional.of(existing));
        when(jobApplicationRepository.save(existing)).thenReturn(existing);

        JobApplication result = jobApplicationService.patch(
                30L,
                new UpdateJobApplicationRequest(null, "Offer", null),
                user
        );

        assertEquals(LocalDate.of(2026, 1, 1), result.getDateApplied());
        assertEquals("Offer", result.getStatus());
        assertEquals(1L, result.getJobEntry().getJobId());
        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(30L, 5L);
        verify(jobApplicationRepository).save(existing);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void patch_whenJobIdProvided_updatesOwnedJobEntry() {
        User user = new User();
        user.setUserId(5L);

        JobEntry originalEntry = new JobEntry();
        originalEntry.setJobId(1L);
        JobEntry replacementEntry = new JobEntry();
        replacementEntry.setJobId(2L);

        JobApplication existing = new JobApplication();
        existing.setJobEntry(originalEntry);

        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(30L, 5L)).thenReturn(Optional.of(existing));
        when(jobEntryRepository.findByJobIdAndUser_UserId(2L, 5L)).thenReturn(Optional.of(replacementEntry));
        when(jobApplicationRepository.save(existing)).thenReturn(existing);

        JobApplication result = jobApplicationService.patch(
                30L,
                new UpdateJobApplicationRequest(2L, null, null),
                user
        );

        assertEquals(2L, result.getJobEntry().getJobId());
        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(30L, 5L);
        verify(jobEntryRepository).findByJobIdAndUser_UserId(2L, 5L);
        verify(jobApplicationRepository).save(existing);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void patch_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(5L);
        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(30L, 5L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
                () -> jobApplicationService.patch(30L, new UpdateJobApplicationRequest(null, "Offer", null), user));

        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(30L, 5L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void delete_whenOwned_deletesById() {
        User user = new User();
        user.setUserId(6L);

        JobApplication existing = new JobApplication();
        existing.setApplicationId(40L);
        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(40L, 6L)).thenReturn(Optional.of(existing));

        jobApplicationService.delete(40L, user);

        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(40L, 6L);
        verify(jobApplicationRepository).deleteById(40L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }

    @Test
    void delete_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(6L);
        when(jobApplicationRepository.findByApplicationIdAndUser_UserId(40L, 6L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobApplicationService.delete(40L, user));

        verify(jobApplicationRepository).findByApplicationIdAndUser_UserId(40L, 6L);
        verifyNoMoreInteractions(jobApplicationRepository, jobEntryRepository);
    }
}
