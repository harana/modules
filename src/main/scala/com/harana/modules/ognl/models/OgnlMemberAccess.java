package com.harana.modules.ognl.models;

import ognl.MemberAccess;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

public class OgnlMemberAccess implements MemberAccess {

    @Override
    public Object setup(Map context, Object target, Member member, String propertyName) {
        Object result = null;
        if (isAccessible(context, target, member, propertyName)) {
            AccessibleObject accessible = (AccessibleObject) member;
            if (!accessible.isAccessible()) {
                result = Boolean.FALSE;
                accessible.setAccessible(true);
            }
        }
        return result;
    }

    @Override
    public void restore(Map context, Object target, Member member, String propertyName, Object state) {
        if (state != null) ((AccessibleObject) member).setAccessible(((Boolean) state));
    }

    @Override
    public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
        return Modifier.isPublic(member.getModifiers());
    }
}