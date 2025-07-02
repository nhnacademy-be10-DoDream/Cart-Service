package shop.dodream.cart.advice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Test
	void handleDataNotFoundException_returns404() throws Exception {
		mockMvc.perform(get("/test/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(content().string("Data not found"));
	}
	
	@Test
	void handleMissingIdentifierException_returns400() throws Exception {
		mockMvc.perform(get("/test/missing-id"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Missing ID"));
	}
	
	@Test
	void handleDuplicationException_returns409() throws Exception {
		mockMvc.perform(get("/test/duplicate"))
				.andExpect(status().isConflict())
				.andExpect(content().string("Duplicate found"));
	}
}
