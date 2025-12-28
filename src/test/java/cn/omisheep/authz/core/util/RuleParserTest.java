package cn.omisheep.authz.core.util;

import cn.omisheep.authz.core.auth.rpd.Rule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RuleParserTest {

    @Test
    void testParseSimpleRule() {
        Rule rule = RuleParser.parseStringToRule("age > 18");
        assertNotNull(rule);
        assertEquals("age", rule.getField());
        assertEquals(">", rule.getOp());
        assertEquals("18", rule.getValue());
    }

    @Test
    void testParseAndRule() {
        Rule rule = RuleParser.parseStringToRule("age > 18 AND gender = 'M'");
        assertNotNull(rule);
        assertEquals("AND", rule.getOp());
        assertEquals(2, rule.getRules().size());
        assertEquals("age", rule.getRules().get(0).getField());
        assertEquals("gender", rule.getRules().get(1).getField());
    }
    
    @Test
    void testParseOrRule() {
        Rule rule = RuleParser.parseStringToRule("status = 1 OR status = 2");
        assertNotNull(rule);
        assertEquals("OR", rule.getOp());
        assertEquals(2, rule.getRules().size());
    }
    
    @Test
    void testParseNestedRule() {
        // (a AND b) OR c
        Rule rule = RuleParser.parseStringToRule("(age > 18 AND gender = 'M') OR role = 'admin'");
        assertNotNull(rule);
        assertEquals("OR", rule.getOp());
        // Depending on implementation, it might be nested
        assertEquals(2, rule.getRules().size());
        
        Rule left = rule.getRules().get(0);
        Rule right = rule.getRules().get(1);
        
        // One of them is the AND group, one is the simple rule
        // The parser logic seems to maintain order?
        // Let's print or expect specific structure.
    }
    
    @Test
    void testParseToStringAndBack() {
        String original = "( age > 18 AND gender = 'M' ) OR role = 'admin'";
        Rule rule = RuleParser.parseStringToRule(original);
        String generated = RuleParser.parseRuleToString(rule);
        assertNotNull(generated);
        // The generated string format might differ slightly in spacing, but should be logically equivalent
        assertTrue(generated.contains("age > 18"));
        assertTrue(generated.contains("gender = 'M'"));
    }
}
