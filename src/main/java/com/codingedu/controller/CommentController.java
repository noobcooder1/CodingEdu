package com.codingedu.controller;

import com.codingedu.entity.Comment;
import com.codingedu.entity.Post;
import com.codingedu.entity.User;
import com.codingedu.repository.CommentRepository;
import com.codingedu.repository.PostRepository;
import com.codingedu.repository.UserRepository;
import com.codingedu.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentController(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // 댓글 등록
    @PostMapping("/community/{postId}/comment")
    public String addComment(@PathVariable(name = "postId") Long postId,
                             @RequestParam(name = "content") String content,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            return "redirect:/login"; // 로그인 안 한 사용자는 튕겨냄
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        // 댓글 엔티티 생성 및 보관
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(author);
        commentRepository.save(comment);

        // 해당 게시글의 댓글 개수 1 증가
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return "redirect:/community/" + postId; // 저장 완료 후 원래 게시글로 다시 이동!
    }
}
