package com.omnixys.logstream.resolvers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class test {

    @QueryMapping
    public String test() {
        log.debug("TEST");
        return "Test!!!";
    }
}
