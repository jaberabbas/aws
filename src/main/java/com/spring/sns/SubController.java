// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package com.spring.sns;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SubController {

    private final SnsService sns;

    @Autowired
    SubController(
            SnsService sns) {
        this.sns = sns;
    }

    @GetMapping("/")
    public String root() {
        return "index";
    }

    @GetMapping("/subscribe")
    public String add() {
        return "sub";
    }

    // Adds a new item to the database.
    @RequestMapping(value = "/addEmail", method = RequestMethod.POST)
    @ResponseBody
    String addItems(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        return sns.subEmail(email);
    }

    @RequestMapping(value = "/delSub", method = RequestMethod.POST)
    @ResponseBody
    String delSub(HttpServletRequest request, HttpServletResponse response) {
        String email = request.getParameter("email");
        sns.unSubEmail(email);
        return email + " was successfully deleted!";
    }

    @RequestMapping(value = "/addMessage", method = RequestMethod.POST)
    @ResponseBody
    String addMessage(HttpServletRequest request, HttpServletResponse response) {
        String body = request.getParameter("body");
        String lang = request.getParameter("lang");
        return sns.pubTopic(body, lang);
    }

    @RequestMapping(value = "/getSubs", method = RequestMethod.GET)
    @ResponseBody
    String getSubs(HttpServletRequest request, HttpServletResponse response) {
        return sns.getAllSubscriptions();
    }
}
