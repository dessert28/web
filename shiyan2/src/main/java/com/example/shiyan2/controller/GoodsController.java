package com.example.shiyan2.controller;

import com.example.shiyan2.model.Goods;
import com.example.shiyan2.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shiyan1")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("/toAdd")
    public String toAdd(Model model) {
        model.addAttribute("goods", new Goods());
        return "addGoods";
    }

    @PostMapping("/addGoods")
    public String addGoods(Goods goods, Model model) {
        goodsService.addGoods(goods);
        model.addAttribute("goodsList", goodsService.findAll());
        return "goodsList";
    }
}