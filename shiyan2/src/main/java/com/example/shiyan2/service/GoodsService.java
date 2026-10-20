package com.example.shiyan2.service;

import com.example.shiyan2.model.Goods;

import java.util.List;

public interface GoodsService {
    void addGoods(Goods goods);

    List<Goods> findAll();
}