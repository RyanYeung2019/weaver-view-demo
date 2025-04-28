package org.weaver;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.weaver.view.util.YamlRecursion;

import jakarta.servlet.http.HttpServletRequest;

public class Utils {
	
	public static String showSampleUrlInDebugLog(HttpServletRequest request) throws Exception {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		YamlRecursion yamlRecursion = new YamlRecursion(resolver.getResource("application.yml").getInputStream());
		Integer configPort = (Integer) yamlRecursion.getYmlMap().get("server.port");
		Integer currentPort = request.getLocalPort();
		String url = request.getRequestURL().toString().replace(currentPort.toString(), configPort.toString());
		String queryString = request.getQueryString();
		return "\n"
				+ "--------\n"
				+ "curl \""+url+"?"+queryString+"\"\n"
				+ "--------";
	}
	
}
