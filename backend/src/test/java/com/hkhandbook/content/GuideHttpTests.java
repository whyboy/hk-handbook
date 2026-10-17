package com.hkhandbook.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:sqlite::memory:", "handbook.preview-enabled=true"})
class GuideHttpTests {
    @Autowired WebApplicationContext context;
    MockMvc http;
    @BeforeEach void setUp() { http = webAppContextSetup(context).build(); }

    @Test void jsonContractKeepsPreviewSeparateAndSerializesDates() throws Exception {
        http.perform(get("/api/guides")).andExpect(status().isOk()).andExpect(content().json("[]"));
        http.perform(get("/api/guides/mtr-route")).andExpect(status().isNotFound());
        http.perform(get("/api/guides/mtr-route?preview=true")).andExpect(status().isOk())
            .andExpect(jsonPath("$.guide.id").value("mtr-route"))
            .andExpect(jsonPath("$.guide.editedOn").value("2026-10-17"))
            .andExpect(jsonPath("$.status").value("unverified"))
            .andExpect(jsonPath("$.verifiedOn").isEmpty());
        http.perform(get("/api/guides?preview=true&q=地铁&category=transport&audience=visitor"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("mtr-route"));
        http.perform(get("/api/guides?category=unknown")).andExpect(status().isBadRequest());
        http.perform(get("/api/guides/missing?preview=true")).andExpect(status().isNotFound());
    }

    @Test void correctionReturnsCreatedOnlyAfterValidSubmission() throws Exception {
        http.perform(post("/api/guides/mtr-route/corrections?preview=true").contentType("application/json")
            .content(""" 
                {"kind":"incorrect","message":"请核查链接","referenceUrl":"https://www.mtr.com.hk/"}
                """))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isString()).andExpect(jsonPath("$.status").value("received"));
        http.perform(post("/api/guides/mtr-route/corrections?preview=true").contentType("application/json")
            .content("{\"kind\":\"other\",\"message\":\"  \"}"))
            .andExpect(status().isBadRequest());
        http.perform(post("/api/guides/mtr-route/corrections?preview=true").contentType("application/json")
            .content("{malformed"))
            .andExpect(status().isBadRequest());
    }
}
