package com.ibm.fixmyship.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.fixmyship.model.Comment;
import com.ibm.fixmyship.model.Dislike;
import com.ibm.fixmyship.model.Like;
import com.ibm.fixmyship.model.Post;
import com.ibm.fixmyship.repository.CommentRepository;
import com.ibm.fixmyship.repository.LikeRepository;
import com.ibm.fixmyship.security.CurrentUser;
import com.ibm.fixmyship.security.UserPrincipal;
import com.ibm.fixmyship.service.CommentService;
import com.ibm.fixmyship.service.DislikeService;
import com.ibm.fixmyship.service.LikeService;
import com.ibm.fixmyship.service.PostService;
import com.ibm.fixmyship.service.UserService;

@RestController
@RequestMapping("/api/post")
public class PostController {

	@Autowired
	private PostService postService;

	@Autowired
	private UserService userService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private LikeService likeService;

	@Autowired
	private DislikeService dislikeService;

	@GetMapping
	public ResponseEntity<List<Post>> getAllPosts() {
		List<Post> list = postService.getAllPosts();
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Post> findPost(@PathVariable Long id) {
		Optional<Post> post = postService.findById(id);
		if (post.isPresent()) {
			return new ResponseEntity<>(post.get(), HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping
	public ResponseEntity<?> addPost(@RequestBody Post post, @CurrentUser UserPrincipal currentUser) {
		//System.out.println(currentUser.getId());
		post.setCommentCount(0L);
		post.setUid(currentUser.getId());
		Post gotPost = postService.save(post);
		if (gotPost == null) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(gotPost, HttpStatus.CREATED);
	}

	@GetMapping("/others")
	public ResponseEntity<?> getPostByOthers(@CurrentUser UserPrincipal currentUser) {
		List<Long> userIds = new ArrayList<>();
		userIds.add(currentUser.getId());
		List<Post> postByOthers = postService.findByUidNotIn(userIds);
		if (postByOthers.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(postByOthers, HttpStatus.OK);
	}

	@GetMapping("/my")
	public ResponseEntity<?> getMyPosts(@CurrentUser UserPrincipal currentUser) {
		List<Long> userIds = new ArrayList<>();
		userIds.add(currentUser.getId());
		List<Post> postByOthers = postService.findByUidIn(userIds);
		if (postByOthers.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(postByOthers, HttpStatus.OK);
	}

	@PostMapping("/comment")
	public ResponseEntity<?> saveComment(@RequestBody Comment comment, @CurrentUser UserPrincipal currentUser) {
		comment.setUid(currentUser.getId());
		comment.setLikeCount(0L);
		comment.setDislikeCount(0L);
		Comment savedComment = commentService.save(comment);
		if (savedComment == null) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		return new ResponseEntity<>(savedComment, HttpStatus.OK);

	}

	@GetMapping("/{pid}/comment")
	public ResponseEntity<?> getCommentsByPostId(@PathVariable Long pid) {
		List<Comment> comments = commentService.findByPid(pid);
		if (comments.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(comments, HttpStatus.OK);
	}

	@PostMapping("/comment/like")
	public ResponseEntity<?> toggleLike(@RequestBody Like like, @CurrentUser UserPrincipal currentUser) {
		like.setUid(currentUser.getId());

		// Checking for existing like with the current Comment id and User id
		Boolean existsLike = likeService.existsByCidAndUid(like.getCid(), like.getUid());
		Boolean existsDisLike = dislikeService.existsByCidAndUid(like.getCid(), like.getUid());

		// If disLike exists for current Comment id and User id
		if (existsDisLike) {

			// Delete dislike
			Long delDislikeCount = dislikeService.deleteByCidAndUid(like.getCid(), like.getUid());

			// If dislike is removed
			if (delDislikeCount >= 1) {
				likeService.save(like);
			}
		}

		// If like exists Remove Like
		else if (existsLike) {
			likeService.deleteByCidAndUid(like.getCid(), like.getUid());
		}

		// if like does not exist . Add the like
		else {
			likeService.save(like);
		}
		Comment updatedComment = commentService.findById(like.getCid());
		List<Long> likeAndDislike = new ArrayList<>();
		likeAndDislike.add(updatedComment.getLikeCount());
		likeAndDislike.add(updatedComment.getDislikeCount());
		return new ResponseEntity<>(likeAndDislike, HttpStatus.OK);
	}

	@PostMapping("/comment/dislike")
	public ResponseEntity<?> toggleDislike(@RequestBody Dislike disLike, @CurrentUser UserPrincipal currentUser){
		disLike.setUid(currentUser.getId());
		
		Boolean existsDisLike = dislikeService.existsByCidAndUid(disLike.getCid(), disLike.getUid());
		Boolean existsLike = likeService.existsByCidAndUid(disLike.getCid(), disLike.getUid());
		
		// If dislike exists Remove dislike
		if(existsDisLike) {
			dislikeService.deleteByCidAndUid(disLike.getCid(), disLike.getUid());
		}
		else if(existsLike) {
			
			// Delete like
			Long delLikeCount = likeService.deleteByCidAndUid(disLike.getCid(), disLike.getUid());
			if(delLikeCount >= 1) {
				dislikeService.save(disLike);
			}
		}
		else {
			dislikeService.save(disLike);
		}
		Comment updatedComment = commentService.findById(disLike.getCid());
		List<Long> likeAndDislike = new ArrayList<>();
		likeAndDislike.add(updatedComment.getLikeCount());
		likeAndDislike.add(updatedComment.getDislikeCount());
		return new ResponseEntity<>(likeAndDislike, HttpStatus.OK);
	}
	
	@GetMapping("/comment/like")
	public ResponseEntity<?> getLikeByUid(@CurrentUser UserPrincipal currentUser){
		List<Long> likesList = likeService.findCidByUid(currentUser.getId());
		if(likesList.isEmpty())
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<>(likesList, HttpStatus.OK);
	}
	
	@GetMapping("/comment/dislike")
	public ResponseEntity<?> getDislikeByUid(@CurrentUser UserPrincipal currentUser){
		List<Long> disLikesList = dislikeService.findCidByUid(currentUser.getId());
		if(disLikesList.isEmpty())
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		return new ResponseEntity<>(disLikesList, HttpStatus.OK);
	}

}