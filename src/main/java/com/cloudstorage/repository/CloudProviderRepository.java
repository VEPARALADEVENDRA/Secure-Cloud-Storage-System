package com.cloudstorage.repository;

import com.cloudstorage.model.CloudProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudProviderRepository extends JpaRepository<CloudProvider, Long> {
}