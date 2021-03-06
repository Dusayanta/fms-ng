package com.ibm.fixmyship.service;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.ibm.fixmyship.model.Like;

public interface LikeService {

	Like save(Like like);

	Boolean existsByCidAndUid(Long cid, Long uid);
	
	Long deleteByCidAndUid(Long cid, Long uid);
	
	List<Like> findByUid(Long uid);
	
	List<Long> findCidByUid(Long uid);
}
