package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.GetJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobEntryRequest;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
import com.group7.jobTrackerApplication.exception.NotAuthenticatedException;
import com.group7.jobTrackerApplication.exception.ResourceNotFoundException;
import com.group7.jobTrackerApplication.model.JobEntry;
import com.group7.jobTrackerApplication.model.User;
import com.group7.jobTrackerApplication.repository.JobEntryRepository;
import com.group7.jobTrackerApplication.service.JobEntryService;
import com.group7.jobTrackerApplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobEntryServiceTests {

    @Mock
    private JobEntryRepository jobEntryRepository;

    @Mock
    private UserService userService;

    private JobEntryService jobEntryService;

    @BeforeEach
    void setUp() {
        jobEntryService = new JobEntryService(jobEntryRepository, userService);
    }

    @Test
    void getAll_whenFound_returnsEntryDtos() {
        User user = new User();
        user.setUserId(1L);

        JobEntry entry = new JobEntry();
        entry.setCompanyName("Acme");
        entry.setJobTitle("Engineer");
        entry.setSalaryText("$100k");
        entry.setPostingURL("https://example.com");

        when(jobEntryRepository.findByUser_UserId(1L)).thenReturn(Optional.of(List.of(entry)));

        List<GetJobEntryRequest> result = jobEntryService.getAll(user);

        assertEquals(1, result.size());
        assertEquals("Acme", result.get(0).companyName());
        assertEquals("Engineer", result.get(0).jobTitle());
        verify(jobEntryRepository).findByUser_UserId(1L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void getAll_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(1L);
        when(jobEntryRepository.findByUser_UserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobEntryService.getAll(user));

        verify(jobEntryRepository).findByUser_UserId(1L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void getById_whenFound_returnsEntryDto() {
        User user = new User();
        user.setUserId(2L);

        JobEntry entry = new JobEntry();
        entry.setCompanyName("NewCo");
        entry.setJobTitle("Backend Engineer");
        entry.setSalaryText("$120k");
        entry.setPostingURL("https://jobs.example.com");

        when(jobEntryRepository.findByJobIdAndUser_UserId(10L, 2L)).thenReturn(Optional.of(entry));

        GetJobEntryRequest result = jobEntryService.getById(10L, user);

        assertEquals("NewCo", result.companyName());
        assertEquals("Backend Engineer", result.jobTitle());
        verify(jobEntryRepository).findByJobIdAndUser_UserId(10L, 2L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void getById_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(2L);
        when(jobEntryRepository.findByJobIdAndUser_UserId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobEntryService.getById(10L, user));

        verify(jobEntryRepository).findByJobIdAndUser_UserId(10L, 2L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void create_whenAuthenticated_savesJobEntry() {
        OAuth2User principal = mock(OAuth2User.class);
        User user = new User();
        user.setUserId(3L);

        when(userService.getOrCreateFromOAuth(principal)).thenReturn(user);
        when(jobEntryRepository.save(any(JobEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateJobEntryRequest request = new CreateJobEntryRequest("Acme", "$100k", "https://example.com", "Engineer");

        JobEntry result = jobEntryService.create(principal, request);

        assertEquals("Acme", result.getCompanyName());
        assertEquals("Engineer", result.getJobTitle());
        assertSame(user, result.getUser());
        verify(userService).getOrCreateFromOAuth(principal);
        verify(jobEntryRepository).save(any(JobEntry.class));
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void create_whenNotAuthenticated_propagatesException() {
        CreateJobEntryRequest request = new CreateJobEntryRequest("Acme", "$100k", "https://example.com", "Engineer");
        when(userService.getOrCreateFromOAuth(null)).thenThrow(new NotAuthenticatedException("Authentication required"));

        assertThrows(NotAuthenticatedException.class, () -> jobEntryService.create(null, request));

        verify(userService).getOrCreateFromOAuth(null);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void replace_whenOwned_updatesAndSaves() {
        User user = new User();
        user.setUserId(4L);

        JobEntry existing = new JobEntry();
        when(jobEntryRepository.findByJobIdAndUser_UserId(20L, 4L)).thenReturn(Optional.of(existing));
        when(jobEntryRepository.save(existing)).thenReturn(existing);

        UpdateJobEntryRequest request = new UpdateJobEntryRequest("NewCo", "NewTitle", "NewSalary", "NewUrl");

        JobEntry result = jobEntryService.replace(20L, request, user);

        assertEquals("NewCo", result.getCompanyName());
        assertEquals("NewTitle", result.getJobTitle());
        assertEquals("NewSalary", result.getSalaryText());
        assertEquals("NewUrl", result.getPostingURL());
        verify(jobEntryRepository).findByJobIdAndUser_UserId(20L, 4L);
        verify(jobEntryRepository).save(existing);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void replace_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(4L);
        when(jobEntryRepository.findByJobIdAndUser_UserId(20L, 4L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobEntryService.replace(20L, new UpdateJobEntryRequest("a", "b", "c", "d"), user));

        verify(jobEntryRepository).findByJobIdAndUser_UserId(20L, 4L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void patch_whenOwned_updatesOnlyNonNullFields() {
        User user = new User();
        user.setUserId(5L);

        JobEntry existing = new JobEntry();
        existing.setCompanyName("OldCo");
        existing.setJobTitle("OldTitle");
        existing.setSalaryText("OldSalary");
        existing.setPostingURL("OldUrl");

        when(jobEntryRepository.findByJobIdAndUser_UserId(30L, 5L)).thenReturn(Optional.of(existing));
        when(jobEntryRepository.save(existing)).thenReturn(existing);

        UpdateJobEntryRequest request = new UpdateJobEntryRequest(null, "PatchedTitle", null, "PatchedUrl");

        JobEntry result = jobEntryService.patch(30L, request, user);

        assertEquals("OldCo", result.getCompanyName());
        assertEquals("PatchedTitle", result.getJobTitle());
        assertEquals("OldSalary", result.getSalaryText());
        assertEquals("PatchedUrl", result.getPostingURL());
        verify(jobEntryRepository).findByJobIdAndUser_UserId(30L, 5L);
        verify(jobEntryRepository).save(existing);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void patch_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(5L);
        when(jobEntryRepository.findByJobIdAndUser_UserId(30L, 5L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobEntryService.patch(30L, new UpdateJobEntryRequest(null, null, null, null), user));

        verify(jobEntryRepository).findByJobIdAndUser_UserId(30L, 5L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void delete_whenOwned_deletesById() {
        User user = new User();
        user.setUserId(6L);

        JobEntry existing = new JobEntry();
        existing.setJobId(40L);
        when(jobEntryRepository.findByJobIdAndUser_UserId(40L, 6L)).thenReturn(Optional.of(existing));

        jobEntryService.delete(40L, user);

        verify(jobEntryRepository).findByJobIdAndUser_UserId(40L, 6L);
        verify(jobEntryRepository).deleteById(40L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void delete_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(6L);
        when(jobEntryRepository.findByJobIdAndUser_UserId(40L, 6L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobEntryService.delete(40L, user));

        verify(jobEntryRepository).findByJobIdAndUser_UserId(40L, 6L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }
}
