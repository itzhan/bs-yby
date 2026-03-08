package com.campus.recruitment.dto;

import lombok.Data;

@Data
public class CompanyProfileDTO {

    private String companyName;

    private String logo;

    /** 互联网/金融/教育/制造/医疗等 */
    private String industry;

    /** 0-50人 / 50-200人 / 200-500人 / 500-1000人 / 1000人以上 */
    private String scale;

    private String address;

    private String city;

    private String description;

    private String website;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    /** 营业执照图片URL */
    private String licenseUrl;
}
