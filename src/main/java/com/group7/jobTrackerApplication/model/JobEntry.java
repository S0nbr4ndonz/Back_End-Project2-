package com.group7.jobTrackerApplication.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_entries")
public class JobEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company")
    private String companyName;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "salary_text")
    private String salaryText;

    @Column(name = "posting_url")
    private String postingURL;

    @OneToOne(mappedBy = "jobEntry", fetch= FetchType.LAZY)
    private JobApplication jobApplication;


    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }


    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getSalaryText() { return salaryText; }
    public void setSalaryText(String salaryText) { this.salaryText = salaryText; }

    public String getPostingURL() { return postingURL; }
    public void setPostingURL(String postingURL) { this.postingURL = postingURL; }

    public JobApplication getJobApplication() {return jobApplication;}
    public void setJobApplication(JobApplication jobApplication) {this.jobApplication =  jobApplication;}
}