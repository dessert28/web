package com.example.shiyan2.service.impl;

import com.example.shiyan2.model.Goods;
import com.example.shiyan2.service.GoodsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {
    private static final List<Goods> GOODS_LIST = new ArrayList<>();
    private static long nextId = 1;

    @Override
    public void addGoods(Goods goods) {
        goods.setId(nextId++);
        GOODS_LIST.add(goods);
    }

    @Override
    public List<Goods> findAll() {
        return GOODS_LIST;
    }
}