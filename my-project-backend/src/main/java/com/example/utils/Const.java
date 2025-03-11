package com.example.utils;

/*常用的常量*/
public class Const {

    /*redis中存JWT黑名单的前缀*/
    public static final String JWT_BLACK_LIST="jwt:blacklist";

    //过滤器优先级
    public static final int ORDER_CORS=-102;
    public static final int ORDER_LIMIT=-101;

    //邮件验证码
    public static final String VERIFY_EMAIL_LIMIT="verify:email:limit";
    public static final String VERIFY_EMAIL_DATA="verify:email:data";



    /*限流时使用的计数key和封禁key*/
    public static final String FLOW_LIMIT_COUNTER = "flow:counter:";
    public static final String FLOW_LIMIT_BLOCK = "flow:block:";

    //请求自定义属性
    public final static String ATTR_USER_ID = "userId";


    //论坛相关
    public final static String FORUM_WEATHER_CACHE="weather:cache:";

    public final static String FORUM_IMAGE_COUNTER="forum:image:";

    public final static String FORUM_TOPIC_CREATE_COUNTER="forum:topic:create";

    public final static String FORUM_TOPIC_COMMENT_COUNTER="forum:topic:comment";

    public final static String FORUM_TOPIC_PREVIEW_CACHE="topic:preview:";

}
