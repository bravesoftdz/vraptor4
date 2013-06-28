package br.com.caelum.vraptor4.interceptor;

import br.com.caelum.vraptor4.AfterCall;
import br.com.caelum.vraptor4.AroundCall;
import br.com.caelum.vraptor4.BeforeCall;

public class BaseInterceptor {

	@BeforeCall
	public void begin(){
		
	}

	@AroundCall
	public void intercept(SimpleInterceptorStack sis){
	}
		

	@AfterCall
	public void after() {
		
	}
}
