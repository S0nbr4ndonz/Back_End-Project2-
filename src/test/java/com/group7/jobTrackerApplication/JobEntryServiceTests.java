package com.group7.jobTrackerApplication;

import com.group7.jobTrackerApplication.DTO.CreateJobEntryRequest;
import com.group7.jobTrackerApplication.DTO.UpdateJobEntryRequest;
import com.group7.jobTrackerApplication.exception.ForbiddenException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobEntryServiceTests {

    @Mock private JobEntryRepository jobEntryRepository;
    @Mock private UserService userService;

    private JobEntryService jobEntryService;

    @BeforeEach
    void setUp() {
        jobEntryService = new JobEntryService(jobEntryRepository, userService);
    }

    // getAll(User) -> can throw -> 2 tests
    @Test
    void getAll_whenFound_returnsEntries() {
        User user = new User();
        user.setUserId(1L);

        List<JobEntry> expected = List.of(new JobEntry(), new JobEntry());
        when(jobEntryRepository.findByUserId(1L)).thenReturn(Optional.of(expected));

        List<JobEntry> result = jobEntryService.getAll(user);

        assertSame(expected, result);
        verify(jobEntryRepository).findByUserId(1L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void getAll_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(1L);

        when(jobEntryRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobEntryService.getAll(user));

        verify(jobEntryRepository).findByUserId(1L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    // getById(Long, User) -> can throw -> 2 tests
    @Test
    void getById_whenFound_returnsEntry() {
        User user = new User();
        user.setUserId(2L);

        JobEntry entry = new JobEntry();
        when(jobEntryRepository.findByJobIdAndUserId(10L, 2L)).thenReturn(Optional.of(entry));

        JobEntry result = jobEntryService.getById(10L, user);

        assertSame(entry, result);
        verify(jobEntryRepository).findByJobIdAndUserId(10L, 2L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void getById_whenMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUserId(2L);

        when(jobEntryRepository.findByJobIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobEntryService.getById(10L, user));

        verify(jobEntryRepository).findByJobIdAndUserId(10L, 2L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    // create(OAuth2User, CreateJobEntryRequest) -> can throw (via userService.getOrCreateFromOAuth) -> 2 tests
    @Test
    void create_whenAuthenticated_savesJobEntry() {
        OAuth2User principal = mock(OAuth2User.class);

        User user = new User();
        user.setUserId(3L);

        when(userService.getOrCreateFromOAuth(principal)).thenReturn(user);

        CreateJobEntryRequest request = mock(CreateJobEntryRequest.class);
        when(request.CompanyName()).thenReturn("Acme");
        when(request.JobTitle()).thenReturn("Engineer");
        when(request.SalaryText()).thenReturn("$100k");
        when(request.PostingUrl()).thenReturn("https://example.com");

        JobEntry saved = new JobEntry();
        when(jobEntryRepository.save(any(JobEntry.class))).thenReturn(saved);

        JobEntry result = jobEntryService.create(principal, request);

        assertSame(saved, result);

        ArgumentCaptor<JobEntry> captor = ArgumentCaptor.forClass(JobEntry.class);
        verify(userService).getOrCreateFromOAuth(principal);
        verify(jobEntryRepository).save(captor.capture());

        JobEntry toSave = captor.getValue();
        assertEquals("Acme", toSave.getCompanyName());
        assertEquals("Engineer", toSave.getJobTitle());
        assertEquals("$100k", toSave.getSalaryText());
        assertEquals("https://example.com", toSave.getPostingURL());
        assertEquals(3L, toSave.getUserId());

        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void create_whenNotAuthenticated_throwsNotAuthenticatedException() {
        // UserService decides what exception; your implementation throws NotAuthenticatedException when principal is null.
        CreateJobEntryRequest request = mock(CreateJobEntryRequest.class);

        assertThrows(RuntimeException.class, () -> jobEntryService.create(null, request));

        verify(userService).getOrCreateFromOAuth(null);
        verifyNoInteractions(jobEntryRepository);
        verifyNoMoreInteractions(userService);
    }

    // replace(Long, UpdateJobEntryRequest, User) -> can throw ForbiddenException -> 2 tests
    @Test
    void replace_whenOwned_updatesAndSaves() {
        User user = new User();
        user.setUserId(4L);

        JobEntry existing = new JobEntry();
        when(jobEntryRepository.findByJobIdAndUserId(20L, 4L)).thenReturn(Optional.of(existing));
        when(jobEntryRepository.save(any(JobEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateJobEntryRequest request = mock(UpdateJobEntryRequest.class);
        when(request.company()).thenReturn("NewCo");
        when(request.jobTitle()).thenReturn("NewTitle");
        when(request.salary()).thenReturn("NewSalary");
        when(request.postingUrl()).thenReturn("NewUrl");

        JobEntry result = jobEntryService.replace(20L, request, user);

        assertEquals("NewCo", result.getCompanyName());
        assertEquals("NewTitle", result.getJobTitle());
        assertEquals("NewSalary", result.getSalaryText());
        assertEquals("NewUrl", result.getPostingURL());

        verify(jobEntryRepository).findByJobIdAndUserId(20L, 4L);
        verify(jobEntryRepository).save(existing);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void replace_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(4L);

        UpdateJobEntryRequest request = mock(UpdateJobEntryRequest.class);
        when(jobEntryRepository.findByJobIdAndUserId(20L, 4L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobEntryService.replace(20L, request, user));

        verify(jobEntryRepository).findByJobIdAndUserId(20L, 4L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    // patch(Long, UpdateJobEntryRequest, User) -> can throw ForbiddenException -> 2 tests
    @Test
    void patch_whenOwned_updatesOnlyNonNullFields() {
        User user = new User();
        user.setUserId(5L);

        JobEntry existing = new JobEntry();
        existing.setCompanyName("OldCo");
        existing.setJobTitle("OldTitle");
        existing.setSalaryText("OldSalary");
        existing.setPostingURL("OldUrl");

        when(jobEntryRepository.findByJobIdAndUserId(30L, 5L)).thenReturn(Optional.of(existing));
        when(jobEntryRepository.save(any(JobEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateJobEntryRequest request = mock(UpdateJobEntryRequest.class);
        when(request.company()).thenReturn(null);
        when(request.jobTitle()).thenReturn("PatchedTitle");
        when(request.salary()).thenReturn(null);
        when(request.postingUrl()).thenReturn("PatchedUrl");

        JobEntry result = jobEntryService.patch(30L, request, user);

        assertEquals("OldCo", result.getCompanyName());
        assertEquals("PatchedTitle", result.getJobTitle());
        assertEquals("OldSalary", result.getSalaryText());
        assertEquals("PatchedUrl", result.getPostingURL());

        verify(jobEntryRepository).findByJobIdAndUserId(30L, 5L);
        verify(jobEntryRepository).save(existing);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void patch_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(5L);

        UpdateJobEntryRequest request = mock(UpdateJobEntryRequest.class);
        when(jobEntryRepository.findByJobIdAndUserId(30L, 5L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobEntryService.patch(30L, request, user));

        verify(jobEntryRepository).findByJobIdAndUserId(30L, 5L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    // delete(Long, User) -> can throw ForbiddenException -> 2 tests
    @Test
    void delete_whenOwned_deletesById() {
        User user = new User();
        user.setUserId(6L);

        JobEntry existing = new JobEntry();
        existing.setJobId(40L);

        when(jobEntryRepository.findByJobIdAndUserId(40L, 6L)).thenReturn(Optional.of(existing));

        jobEntryService.delete(40L, user);

        verify(jobEntryRepository).findByJobIdAndUserId(40L, 6L);
        verify(jobEntryRepository).deleteById(40L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }

    @Test
    void delete_whenNotOwned_throwsForbiddenException() {
        User user = new User();
        user.setUserId(6L);

        when(jobEntryRepository.findByJobIdAndUserId(40L, 6L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> jobEntryService.delete(40L, user));

        verify(jobEntryRepository).findByJobIdAndUserId(40L, 6L);
        verifyNoMoreInteractions(jobEntryRepository, userService);
    }
}