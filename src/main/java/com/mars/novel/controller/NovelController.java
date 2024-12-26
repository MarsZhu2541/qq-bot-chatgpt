//package com.mars.novel.controller;
//
//import com.mars.novel.model.Book;
//import com.mars.novel.model.Chapter;
//import com.mars.novel.model.Rule;
//import com.mars.novel.model.SearchResult;
//import com.mars.novel.service.NovelServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//@RequestMapping("/novel")
//public class NovelController {
//
//    @Autowired
//    private NovelServiceImpl novelService;
//
//    @Autowired
//    List<Rule> rules;
//
//    private List<SearchResult> searchResult = new ArrayList<>();
//    private Book book;
//
//    @GetMapping("/search")
//    public ResponseEntity<List<SearchResult>> searchNovelName(@RequestParam(value = "keyword", required = true) String keyword) {
//        this.searchResult = novelService.searchBooks(keyword);
//        return ResponseEntity.ok(this.searchResult);
//    }
//
//    @GetMapping("/bookDetail")
//    public ResponseEntity<Book> getBookDetail(@RequestParam(value = "index", required = true) int index) {
//        this.book = novelService.getBookDetail(this.searchResult.get(index));
//        return ResponseEntity.ok(book);
//    }
//
//
//    @GetMapping("/chapter")
//    public ResponseEntity<Chapter> getChapterContent(@RequestParam(value = "index", required = true) int index) {
//        return ResponseEntity.ok(novelService.getChapterContent(book.getCatalog().get(index - 1)));
//    }
//
//
//
//}
