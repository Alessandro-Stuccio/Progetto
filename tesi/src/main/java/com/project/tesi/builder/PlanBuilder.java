package com.project.tesi.builder;

import com.project.tesi.enums.PlanDuration;
import com.project.tesi.model.*;


public interface PlanBuilder {
    PlanBuilder id(Long id);
    PlanBuilder name(String name);
    PlanBuilder duration(PlanDuration duration);
    PlanBuilder fullPrice(Double fullPrice);
    PlanBuilder monthlyInstallmentPrice(Double monthlyInstallmentPrice);
    PlanBuilder monthlyCreditsPT(int monthlyCreditsPT);
    PlanBuilder monthlyCreditsNutri(int monthlyCreditsNutri);
    Plan build();
}
