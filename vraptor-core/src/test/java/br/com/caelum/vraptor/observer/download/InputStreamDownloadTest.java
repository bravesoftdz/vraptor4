/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package br.com.caelum.vraptor.observer.download;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.com.caelum.vraptor.observer.download.InputStreamDownload;

public class InputStreamDownloadTest {

	private InputStream inputStream;
	private byte[] bytes;
	private @Mock HttpServletResponse response;
	private ServletOutputStream socketStream;
	private ByteArrayOutputStream outputStream;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		bytes = new byte[] { (byte) 0 };
		this.inputStream = new ByteArrayInputStream(bytes);
		this.outputStream = new ByteArrayOutputStream();

		this.socketStream = new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				outputStream.write(b);
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {
			}
		};

		when(response.getOutputStream()).thenReturn(socketStream);
	}

	@Test
	public void shouldFlushWholeStreamToHttpResponse() throws IOException {
		InputStreamDownload fd = new InputStreamDownload(inputStream, "type", "x.txt");
		fd.write(response);
		
		assertArrayEquals(bytes, outputStream.toByteArray());
	}

	@Test
	public void shouldUseHeadersToHttpResponse() throws IOException {
		InputStreamDownload fd = new InputStreamDownload(inputStream, "type", "x.txt");
		fd.write(response);

		verify(response).setHeader("Content-type", "type");
		assertArrayEquals(bytes, outputStream.toByteArray());
	}

}
