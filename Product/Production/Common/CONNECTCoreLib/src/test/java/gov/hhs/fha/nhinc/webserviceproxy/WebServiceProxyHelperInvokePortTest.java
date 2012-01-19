package gov.hhs.fha.nhinc.webserviceproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import gov.hhs.fha.nhinc.properties.PropertyAccessException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.jmock.Expectations;
import org.junit.Test;

public class WebServiceProxyHelperInvokePortTest extends
		AbstractWebServiceProxyHelpTest {

	/**
	 * This method is used to test out some of the dynamic invocaton methods.
	 * 
	 * @param x
	 *            an integer.
	 * @param y
	 *            an integer.
	 * @param a
	 *            result.
	 */
	public Integer helperMethod2(Integer x, Integer y) {
		return x;
	}

	/**
	 * This method is used to test out some of the dynamic invocaton methods.
	 * 
	 * @param x
	 *            an integer.
	 * @param a
	 *            result.
	 */
	public Integer helperMethod(Integer x) {
		return x;
	}

	public Integer exceptionalMethod(Integer x) throws Exception {
		throw new SocketTimeoutException("SocketTimeoutException");
	}

	public Integer exceptionalWSMethod(Integer x) throws Exception {
		throw new WebServiceException("WebServiceExpcetion");
	}

	/**
	 * Test the getMethod method.
	 */
	@Test
	public void testGetMethod() {
		Method oMethod = oHelper.getMethod(this.getClass(), "helperMethod");
		assertNotNull("getMethod failed", oMethod);
		assertEquals("Incorrect method returned.", "helperMethod",
				oMethod.getName());

	}

	/**
	 * Test the invokePort method happy path.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvokePortHappyPath() throws Exception {
		context.checking(new Expectations() {

			{
				ignoring(mockLog).debug(with(any(String.class)));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYATTEMPTS);
				will(returnValue("0"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYDELAY);
				will(returnValue("0"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_EXCEPTION);
				will(returnValue("SocketTimeoutException"));

			}
		});

		Integer oResponse = (Integer) oHelper.invokePort(this, this.getClass(),
				"helperMethod", new Integer(100));
		assertNotNull("invokePort failed to return a value.", oResponse);
		assertTrue("Response was incorrect type.", oResponse instanceof Integer);
		assertEquals("Incorrect value returned.", 100, oResponse.intValue());

	}

	/**
	 * Test the invokePort method illegal argument exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInvokePortIllegalArgumentException() throws Exception {
		context.checking(new Expectations() {

			{
				ignoring(mockLog).debug(with(any(String.class)));
				oneOf(mockLog).error(with(any(String.class)),
						with(any(WebServiceException.class)));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYATTEMPTS);
				will(returnValue("0"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYDELAY);
				will(returnValue("10"));

				exactly(3).of(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_EXCEPTION);
				will(returnValue("SocketTimeoutException"));

			}
		});

		oHelper.invokePort(this, this.getClass(),
				"helperMethod2", new Integer(100));

	}

	/**
	 * Test the invokePort method with retry settings with exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = SocketTimeoutException.class)
	public void testInvokePortWithInvocationTargetException() throws Exception {

		context.checking(new Expectations() {

			{
				ignoring(mockLog).debug(with(any(String.class)));
				oneOf(mockLog).error(with(any(String.class)),
						with(any(SocketTimeoutException.class)));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYATTEMPTS);
				will(returnValue("0"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYDELAY);
				will(returnValue("0"));

				exactly(3).of(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_EXCEPTION);
				will(returnValue("SocketTimeoutException"));

			}
		});

		 oHelper.invokePort(this, this.getClass(),
				"exceptionalMethod", 100);

	}

	/**
	 * Test the invokePort method with retry settings happy path.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvokePortRetrySettingsHappyPath() throws Exception {
		context.checking(new Expectations() {

			{
				ignoring(mockLog).debug(with(any(String.class)));
				oneOf(mockLog).error(with(any(String.class)),
						with(any(WebServiceException.class)));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYATTEMPTS);
				will(returnValue("3"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYDELAY);
				will(returnValue("10"));

				exactly(3).of(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_EXCEPTION);
				will(returnValue("javax.xml.ws.WebServiceException"));

			}
		});

		Integer oResponse = (Integer) oHelper.invokePort(this, this.getClass(),
				"helperMethod", new Integer(100));
		assertNotNull("invokePort failed to return a value.", oResponse);
		assertTrue("Response was incorrect type.", oResponse instanceof Integer);

	}

	/**
	 * Test the invokePort method with retry settings with exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = WebServiceException.class)
	public void testInvokePortRetrySettingsWithWebServiceException()
			throws Exception {
		context.checking(new Expectations() {

			{
				ignoring(mockLog).debug(with(any(String.class)));

				exactly(3).of(mockLog).error(with(any(String.class)),
						with(any(WebServiceException.class)));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYATTEMPTS);
				will(returnValue("3"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYDELAY);
				will(returnValue("10"));

				exactly(3).of(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_EXCEPTION);
				will(returnValue("javax.xml.ws.WebServiceException"));

			}
		});

		Integer oResponse = (Integer) oHelper.invokePort(this, this.getClass(),
				"exceptionalWSMethod", new Integer(100));

	}

	/**
	 * Test the invokePort method with retry settings with exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInvokePortRetrySettingsWithWebServiceExceptionNoTextMatch()
			throws Exception {
		context.checking(new Expectations() {

			{
				ignoring(mockLog).debug(with(any(String.class)));
				oneOf(mockLog).error(with(any(String.class)),
						with(any(WebServiceException.class)));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYATTEMPTS);
				will(returnValue("3"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYDELAY);
				will(returnValue("10"));

				exactly(3).of(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_EXCEPTION);
				will(returnValue("javax.xml.ws.WebServiceException"));
			}
		});

		oHelper.invokePort(this, this.getClass(), "badMethodName", new Integer(
				100));

	}

	/**
	 * Test the invokePort method with retry settings with exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInvokePortRetrySettingsWithIllegalArgumentException()
			throws Exception {
		context.checking(new Expectations() {

			{
				ignoring(mockLog).debug(with(any(String.class)));
				oneOf(mockLog).error(with(any(String.class)),
						with(any(WebServiceException.class)));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYATTEMPTS);
				will(returnValue("3"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYDELAY);
				will(returnValue("10"));

				exactly(3).of(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_EXCEPTION);
				will(returnValue("javax.xml.ws.WebServiceException"));
			}
		});

		oHelper.invokePort(this, this.getClass(), "exceptionalMethod", "100");
	}

	/**
	 * Test the invokePort method with retry settings with exception.
	 * 
	 * @throws Exception
	 */
	@Test(expected = WebServiceException.class)
	public void testInvokePortRetrySettingsWithInvocationTargetException()
			throws Exception {

		context.checking(new Expectations() {

			{
				ignoring(mockLog).debug(with(any(String.class)));
				exactly(3).of(mockLog).error(with(any(String.class)),
						with(any(WebServiceException.class)));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYATTEMPTS);
				will(returnValue("3"));

				oneOf(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_RETRYDELAY);
				will(returnValue("10"));

				exactly(3).of(mockPropertyAccessor).getProperty(
						WebServiceProxyHelper.CONFIG_KEY_EXCEPTION);
				will(returnValue("javax.xml.ws.WebServiceException"));

			}
		});

		oHelper.invokePort(this, this.getClass(),
				"exceptionalWSMethod", 100);
	}

}
