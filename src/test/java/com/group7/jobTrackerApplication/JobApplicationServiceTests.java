package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateJobApplicationRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobApplicationRequest;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.JobApplication;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobApplicationRepository;
import com.group7.jobTrackerApplication.service.JobApplicationService;
import com.group7.jobTrackerApplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTests {

    @Mock private JobApplicationRepository jobApplicationRepository;
    @Mock private UserService userService; // exists in constructor but not used in your methods right now

    private JobApplicationService jobApplicationService;

    @BeforeEach
    void setUp() {
        jobApplicationService = new JobApplicationService(jobApplicationRepository, userService);
    }

    // getAll(User) -> can throw -> 2 tests
    @Test
    void getAll_whenFound_returnsApplications() {
        User user = new User();
        user.setUserId(1L);

        List<JobApplication> expected = List.of(new JobApplication());
        when(jobApplicationRepository.findByUserId(1L)).thenReturn(Optional.of(expected));

        List<JobApplication> result = jobApplicationService.getAll(user);

        assertSame(expected, result);
        verify(jobApplicationRepository).findByUserId(1L);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    @Test
    void getAll_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(1L);

        when(jobApplicationRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobApplicationService.getAll(user));

        verify(jobApplicationRepository).findByUserId(1L);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    // getById(Long, User) -> can throw -> 2 tests
    @Test
    void getById_whenFound_returnsApplication() {
        User user = new User();
        user.setUserId(2L);

        JobApplication app = new JobApplication();
        when(jobApplicationRepository.findByApplicationIdAndUserId(10L, 2L)).thenReturn(Optional.of(app));

        JobApplication result = jobApplicationService.getById(10L, user);

        assertSame(app, result);
        verify(jobApplicationRepository).findByApplicationIdAndUserId(10L, 2L);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    @Test
    void getById_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(2L);

        when(jobApplicationRepository.findByApplicationIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobApplicationService.getById(10L, user));

        verify(jobApplicationRepository).findByApplicationIdAndUserId(10L, 2L);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    // create(CreateJobApplicationRequest, User) -> no exception -> 1 test
    @Test
    void create_savesNewApplication() {
        User user = new User();
        user.setUserId(3L);

        CreateJobApplicationRequest request = mock(CreateJobApplicationRequest.class);
        LocalDate date = LocalDate.of(2026, 1, 1);
        when(request.dateApplied()).thenReturn(date);
        when(request.status()).thenReturn("Applied");
        when(request.jobId()).thenReturn(99L);

        JobApplication saved = new JobApplication();
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(saved);

        JobApplication result = jobApplicationService.create(request, user);

        assertSame(saved, result);

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(jobApplicationRepository).save(captor.capture());

        JobApplication toSave = captor.getValue();
        assertEquals(date, toSave.getDateApplied());
        assertEquals("Applied", toSave.getStatus());
        assertEquals(3L, toSave.getUserId());
        assertEquals(99L, toSave.getJobId());

        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    // replace(Long, UpdateJobApplicationRequest, User) -> can throw ForbiddenException -> 2 tests
    @Test
    void replace_whenOwned_updatesAndSaves() {
        User user = new User();
        user.setUserId(4L);

        JobApplication existing = new JobApplication();
        when(jobApplicationRepository.findByApplicationIdAndUserId(20L, 4L)).thenReturn(Optional.of(existing));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateJobApplicationRequest request = mock(UpdateJobApplicationRequest.class);
        LocalDate date = LocalDate.of(2026, 2, 2);
        when(request.dataApplied()).thenReturn(date);
        when(request.status()).thenReturn("Interview");
        when(request.jobId()).thenReturn(123L);

        JobApplication result = jobApplicationService.replace(20L, request, user);

        assertEquals(date, result.getDateApplied());
        assertEquals("Interview", result.getStatus());
        assertEquals(123L, result.getJobId());

        verify(jobApplicationRepository).findByApplicationIdAndUserId(20L, 4L);
        verify(jobApplicationRepository).save(existing);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    @Test
    void replace_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(4L);

        UpdateJobApplicationRequest request = mock(UpdateJobApplicationRequest.class);
        when(jobApplicationRepository.findByApplicationIdAndUserId(20L, 4L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobApplicationService.replace(20L, request, user));

        verify(jobApplicationRepository).findByApplicationIdAndUserId(20L, 4L);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    // patch(Long, UpdateJobApplicationRequest, User) -> can throw ForbiddenException -> 2 tests
    @Test
    void patch_whenOwned_updatesOnlyNonNullFields() {
        User user = new User();
        user.setUserId(5L);

        JobApplication existing = new JobApplication();
        existing.setStatus("Applied");
        existing.setJobId(1L);
        existing.setDateApplied(LocalDate.of(2026, 1, 1));

        when(jobApplicationRepository.findByApplicationIdAndUserId(30L, 5L)).thenReturn(Optional.of(existing));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateJobApplicationRequest request = mock(UpdateJobApplicationRequest.class);
        when(request.dataApplied()).thenReturn(null);
        when(request.status()).thenReturn("Offer");
        when(request.jobId()).thenReturn(null);

        JobApplication result = jobApplicationService.patch(30L, request, user);

        assertEquals(LocalDate.of(2026, 1, 1), result.getDateApplied());
        assertEquals("Offer", result.getStatus());
        assertEquals(1L, result.getJobId());

        verify(jobApplicationRepository).findByApplicationIdAndUserId(30L, 5L);
        verify(jobApplicationRepository).save(existing);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    @Test
    void patch_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(5L);

        UpdateJobApplicationRequest request = mock(UpdateJobApplicationRequest.class);
        when(jobApplicationRepository.findByApplicationIdAndUserId(30L, 5L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobApplicationService.patch(30L, request, user));

        verify(jobApplicationRepository).findByApplicationIdAndUserId(30L, 5L);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    // delete(Long, User) -> can throw ForbiddenException -> 2 tests
    @Test
    void delete_whenOwned_deletesById() {
        User user = new User();
        user.setUserId(6L);

        JobApplication existing = new JobApplication();
        existing.setApplicationId(40L);

        when(jobApplicationRepository.findByApplicationIdAndUserId(40L, 6L)).thenReturn(Optional.of(existing));

        jobApplicationService.delete(40L, user);

        verify(jobApplicationRepository).findByApplicationIdAndUserId(40L, 6L);
        verify(jobApplicationRepository).deleteById(40L);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }

    @Test
    void delete_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(6L);

        when(jobApplicationRepository.findByApplicationIdAndUserId(40L, 6L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobApplicationService.delete(40L, user));

        verify(jobApplicationRepository).findByApplicationIdAndUserId(40L, 6L);
        verifyNoMoreInteractions(jobApplicationRepository, userService);
    }
}