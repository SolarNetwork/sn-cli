package s10k.tool.common.util;

import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;

import net.solarnetwork.common.expr.spel.SpelExpressionService;
import net.solarnetwork.service.ExpressionService;

/**
 * Utilities for expressions.
 */
public final class ExpressionUtils {

	private static final SpelExpressionService EXPRESSION_SERVICE = new SpelExpressionService();

	private ExpressionUtils() {
		// not available
	}

	public static ExpressionService spelExpressionService() {
		return EXPRESSION_SERVICE;
	}

	/**
	 * Get a parsed {@link Expression} instance for an expression.
	 * 
	 * @param expression the expression to parse
	 * @return the parsed expression
	 * @throws ExpressionException if any error occurs
	 */
	public static Expression spelExpression(String expression) throws ExpressionException {
		return spelExpressionService().parseExpression(expression);
	}

	/**
	 * Evaluate a parsed expression.
	 * 
	 * @param <T>         the result type
	 * @param expression  the parsed expression to evaluate
	 * @param variables   optional variables to include
	 * @param root        the root object
	 * @param resultClass the result class
	 * @return the result
	 * @throws ExpressionException if any error occurs
	 */
	public static <T> @Nullable T evaluateExpression(Expression expression, @Nullable Map<String, Object> variables,
			@Nullable Object root, Class<T> resultClass) {
		return spelExpressionService().evaluateExpression(expression, variables, root, null, resultClass);
	}

	/**
	 * Parse and evaluate an expression.
	 * 
	 * @param <T>         the result type
	 * @param expression  the expression to parse and evaluate
	 * @param variables   optional variables to include
	 * @param root        the root object
	 * @param resultClass the result class
	 * @return the result
	 * @throws ExpressionException if any error occurs
	 */
	public static <T> @Nullable T evaluateSpelExpression(String expression, @Nullable Map<String, Object> variables,
			@Nullable Object root, Class<T> resultClass) {
		return evaluateExpression(spelExpression(expression), variables, root, resultClass);
	}

}
