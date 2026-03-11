package com.codingedu.controller;

import com.codingedu.entity.Post;
import com.codingedu.entity.User;
import com.codingedu.repository.CommentRepository;
import com.codingedu.repository.PostRepository;
import com.codingedu.repository.UserRepository;
import com.codingedu.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/community")
public class CommunityController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public CommunityController(PostRepository postRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    // 1. 커뮤니티 목록 보기
    @GetMapping
    public String list(@RequestParam(name = "category", required = false, defaultValue = "all") String category, Model model) {
        List<Post> posts;
        if ("all".equals(category)) {
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        } else {
            posts = postRepository.findByCategoryOrderByCreatedAtDesc(category);
        }
        
        model.addAttribute("posts", posts);
        model.addAttribute("currentCategory", category);
        return "community"; // templates/community.html
    }

    // 2. 글 작성 폼 보여주기
    @GetMapping("/write")
    public String writeForm(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login"; // 로그인 안 한 상태면 로그인 창으로
        }
        return "community-write"; // templates/community-write.html
    }

    // 3. 폼 제출 시 실제 DB에 글 저장하기
    @PostMapping("/write")
    public String writeProcess(@RequestParam(name = "category") String category,
                               @RequestParam(name = "title") String title,
                               @RequestParam(name = "content") String content,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 로그인한 회원 엔티티 가져오기
        User author = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 게시글 생성 및 작성자 저장
        Post post = new Post();
        post.setCategory(category);
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(author);

        postRepository.save(post);

        return "redirect:/community"; // 글 작성 완료 후 목록으로 이동
    }

    // 4. 글 상세보기 (조회수 증가 포함)
    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 조회수 1 증가시키고 저장
        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        model.addAttribute("post", post);
        model.addAttribute("comments", commentRepository.findByPostIdOrderByCreatedAtAsc(id));
        return "community-detail"; // templates/community-detail.html
    }
}
