package com.chat.myAgent.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 简历项目描述视图
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeProjectVO {
    private String projectName;
    private String summary;
    private List<String> highlights;
    private List<String> techStack;
    private List<String> responsibilities;
}
