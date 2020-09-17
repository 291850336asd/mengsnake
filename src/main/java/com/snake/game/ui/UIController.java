package com.snake.game.ui;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/play")
public class UIController {


    @RequestMapping("/muti")
    public ModelAndView getPlayUI(){
        ModelAndView  modelAndView= new ModelAndView("snake/SnakeGamePage");
        return modelAndView;
    }

    @GetMapping("/single")
    @ResponseBody
    public ModelAndView singlePlay(){
        ModelAndView  modelAndView= new ModelAndView("snake/SnakeJsDemo");
        return modelAndView;
    }
}
