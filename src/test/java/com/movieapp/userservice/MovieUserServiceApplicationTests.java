package com.movieapp.userservice;



import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.movieapp.userservice.Calculator.*;


class MovieUserServiceApplicationTests {



	@Test
	void contextLoads() {



	}

	@Test
	void addTest() {
		int a = 5,b = 6;
		int res = add(a,b);
		assertThat(res).isEqualTo(11);
	}

    @Test
	void subTest() {
		int a = 5,b = 6;
		int res = sub(a,b);
		assertThat(res).isEqualTo(-1);
	}

	@Test
	void mulTest() {
		int a = 5,b = 6;
		int res = mul(a,b);
		assertThat(res).isEqualTo(30);
	}

	@Test
	void divTest() {
		int a = 10,b = 2;
		int res = sub(a,b);
		assertThat(res).isEqualTo(8);
	}

	@Test
	void divideNumberWithZeroTest() {
		int a = 5,b = 0;
		assertThatThrownBy(() -> div(a,b))
				.isInstanceOf(ArithmeticException.class)
				.hasMessage("Cannot divide with zero");
	}


}
