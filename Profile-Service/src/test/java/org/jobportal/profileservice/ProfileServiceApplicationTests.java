package org.jobportal.profileservice;

import org.jobportal.profileservice.dto.AddressDto;
import org.jobportal.profileservice.dto.CandidateProfileDto;
import org.jobportal.profileservice.dto.RecruiterProfileDto;
import org.jobportal.profileservice.entity.*;
import org.jobportal.profileservice.repository.ProfileRepository;
import org.jobportal.profileservice.service.ProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.cache.type=none"})
class ProfileServiceApplicationTests {

    @Autowired
    private ProfileServiceImpl profileService;

    @MockitoBean
    private ProfileRepository repository;

    private CandidateProfileDto candidateDto;
    private RecruiterProfileDto recruiterDto;
    private AddressDto addressDto;

    @BeforeEach
    void setUp() {
        addressDto = new AddressDto();
        addressDto.setCity("Pune");
        addressDto.setState("Maharashtra");
        addressDto.setPincode(411001);

        candidateDto = new CandidateProfileDto();
        candidateDto.setFullName("John Doe");
        candidateDto.setEmail("john@test.com");
        candidateDto.setAddress(addressDto);
        candidateDto.setSkills(List.of("Java", "Spring Boot"));

        recruiterDto = new RecruiterProfileDto();
        recruiterDto.setFullName("Jane Recruiter");
        recruiterDto.setEmail("jane@company.com");
        recruiterDto.setAddress(addressDto);
        recruiterDto.setCompanyName("Tech Corp");
    }

    @Test
    void contextLoads() {
        assertNotNull(profileService);
    }

    @Test
    void testAddCandidateProfile_Success() {
        CandidateProfile savedEntity = new CandidateProfile();
        savedEntity.setProfileId(1L);
        savedEntity.setFullName("John Doe");

        when(repository.save(any(CandidateProfile.class))).thenReturn(savedEntity);
        UserProfile result = profileService.addCandidateProfile(candidateDto);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        verify(repository, times(1)).save(any(CandidateProfile.class));
    }

    @Test
    void testAddRecruiterProfile_Success() {
        RecruiterProfile savedEntity = new RecruiterProfile();
        savedEntity.setProfileId(2L);
        savedEntity.setCompanyName("Tech Corp");

        when(repository.save(any(RecruiterProfile.class))).thenReturn(savedEntity);
        UserProfile result = profileService.addRecruiterProfile(recruiterDto);

        assertNotNull(result);
        assertTrue(result instanceof RecruiterProfile);
        assertEquals("Tech Corp", ((RecruiterProfile) result).getCompanyName());
        verify(repository, times(1)).save(any(RecruiterProfile.class));
    }

    @Test
    void testGetProfileByEmail_Success() {
        CandidateProfile profile = new CandidateProfile();
        profile.setEmail("john@test.com");
        when(repository.findByEmail("john@test.com")).thenReturn(Optional.of(profile));

        UserProfile result = profileService.getProfileByEmail("john@test.com");

        assertEquals("john@test.com", result.getEmail());
    }

    @Test
    void testUpdateProfile_Candidate_Success() {
        Long id = 1L;
        CandidateProfile existingProfile = new CandidateProfile();
        existingProfile.setProfileId(id);
        existingProfile.setFullName("Old Name");
        existingProfile.setRole(Role.CANDIDATE);
        existingProfile.setAddress(new Address());

        when(repository.findById(id)).thenReturn(Optional.of(existingProfile));
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        CandidateProfileDto updateDto = new CandidateProfileDto();
        updateDto.setFullName("New Name");
        updateDto.setSkills(List.of("Python"));

        UserProfile result = profileService.updateProfile(id, updateDto);

        assertEquals("New Name", result.getFullName());
        assertEquals(List.of("Python"), ((CandidateProfile)result).getSkills());
        verify(repository, times(1)).save(any());
    }

    @Test
    void testUpdateProfile_Recruiter_Success() {
        Long id = 2L;
        RecruiterProfile existingProfile = new RecruiterProfile();
        existingProfile.setProfileId(id);
        existingProfile.setCompanyName("Old Company");
        existingProfile.setRole(Role.RECRUITER);
        existingProfile.setAddress(new Address());

        when(repository.findById(id)).thenReturn(Optional.of(existingProfile));
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        RecruiterProfileDto updateDto = new RecruiterProfileDto();
        updateDto.setCompanyName("New Company");

        UserProfile result = profileService.updateProfile(id, updateDto);

        assertEquals("New Company", ((RecruiterProfile)result).getCompanyName());
    }

    @Test
    void testDeleteProfile_Success() {
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);

        profileService.deleteProfile(id);

        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteProfile_NotFound_ShouldThrowException() {
        Long id = 99L;
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> profileService.deleteProfile(id));
        verify(repository, never()).deleteById(id);
    }

    @Test
    void testGetAllProfiles_ShouldReturnList() {
        when(repository.findAll()).thenReturn(List.of(new CandidateProfile(), new RecruiterProfile()));
        List<UserProfile> result = profileService.getAllProfiles();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }
}