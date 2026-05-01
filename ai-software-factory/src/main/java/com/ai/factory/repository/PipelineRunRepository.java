package com.ai.factory.repository;

import com.ai.factory.model.PipelineRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {
}
