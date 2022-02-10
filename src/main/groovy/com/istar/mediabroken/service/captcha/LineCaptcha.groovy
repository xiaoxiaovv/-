package com.istar.mediabroken.service.captcha

import cn.hutool.captcha.AbstractCaptcha
import cn.hutool.core.util.ImageUtil
import cn.hutool.core.util.RandomUtil

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.concurrent.ThreadLocalRandom

/**
 * Author : YCSnail
 * Date   : 2018-03-15
 * Email  : liyancai1986@163.com
 */
class LineCaptcha extends AbstractCaptcha {

    private static final long serialVersionUID = 8691294460763091089L;
    private static final String BASE_CHAR_NUMBER = "abcdefghjkmnpqrstuvwxyz" + "ABCDEFGHIJKLMNPRSTUVWXYZ" + "23456789";

    // -------------------------------------------------------------------- Constructor start
    /**
     * 构造，默认5位验证码，150条干扰线
     *
     * @param width 图片宽
     * @param height 图片高
     */
    public LineCaptcha(int width, int height) {
        this(width, height, 5, 150);
    }

    /**
     * 构造
     *
     * @param width 图片宽
     * @param height 图片高
     * @param codeCount 字符个数
     * @param lineCount 干扰线条数
     */
    public LineCaptcha(int width, int height, int codeCount, int lineCount) {
        super(width, height, codeCount, lineCount);
    }
    // -------------------------------------------------------------------- Constructor end

    @Override
    public void createCode() {
        this.generateCode();
        createImage(this.code);
    }

    protected void generateCode() {
        this.code = RandomUtil.randomString(BASE_CHAR_NUMBER, this.codeCount);
    }

    @Override
    public void createImage(String code) {
        // 图像buffer
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final ThreadLocalRandom random = RandomUtil.getRandom();
        final Graphics2D g = ImageUtil.createGraphics(this.image, this.randomColor(random, 200, 250));
        // 创建字体
        g.setFont(this.font);

        // 干扰线
        drawInterfere(g, random);

        // 文字
        int charWidth = width / codeCount;
        for (int i = 0; i < codeCount; i++) {
            // 产生随机的颜色值，让输出的每个字符的颜色值都将不同。
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));

            String s = String.valueOf(code.charAt(i))
            int x = i * charWidth
            int y = s.toLowerCase().equals(s) ? (height / 2) + 5 : height - 10

            g.drawString(s, x, y);
        }
    }

    /**
     * 绘制干扰线
     *
     * @param g {@link Graphics2D}画笔
     * @param random 随机对象
     */
    private void drawInterfere(Graphics2D g, ThreadLocalRandom random) {
        // 干扰线
        for (int i = 0; i < this.interfereCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(BigDecimal.valueOf(width / 8).intValue());
            int ye = ys + random.nextInt(BigDecimal.valueOf(height / 8).intValue());
            g.setColor(ImageUtil.randomColor(random));
            g.drawLine(xs, ys, xe, ye);
        }
    }

    private Color randomColor(ThreadLocalRandom random, int min, int max) {
        return new Color(random.nextInt(max - min) + min, random.nextInt(max - min) + min, random.nextInt(max - min) + min);
    }

}
