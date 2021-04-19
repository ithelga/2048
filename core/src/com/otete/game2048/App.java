package com.otete.game2048;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;


import java.util.Arrays;


public class App extends ApplicationAdapter implements InputProcessor {

    private static final int FONT_SIZE = 70, S_RIGHT = 1, S_LEFT = 2, S_UP = 3, S_DOWN = 4, SIZE = 4;

    public static final int Menu = 1, Game = 2;
    public static Color clBackground = Color.valueOf("F3F1E6");
    public static Color clText = Color.valueOf("643C3C");
    public static Stage stage;
    public static BitmapFont font;
    public static float AR;
    public static int WIDTH, HEIGHT, HALF_WIDHT, HALF_HEIGHT;

    private Preferences prefs;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Texture notice, field, but1, but2, iconStart, iconExit;
    private Actor butStart, butExit, butMenu, butRestart;

    private int[][] state;
    private Texture[] steps;

    private boolean isSwiped, isEnd;
    private int screen, score = 0, record = 0, downX, downY;

    @Override
    public void create() {
        HEIGHT = 1920;
        WIDTH = HEIGHT * Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
        if (WIDTH < 1080) {
            WIDTH = 1080;
            HEIGHT = WIDTH * Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
        }
        HALF_WIDHT = WIDTH / 2;
        HALF_HEIGHT = HEIGHT / 2;
        AR = (float) WIDTH / Gdx.graphics.getWidth();

        camera = new OrthographicCamera();
        stage = new Stage(new StretchViewport(WIDTH, HEIGHT, camera));
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        field = new Texture("images/field.png");
        notice = new Texture("images/notice.png");

        but1 = new Texture("images/but1.png");
        but2 = new Texture("images/but2.png");
        iconStart = new Texture("images/icon_start.png");
        iconExit = new Texture("images/icon_exit.png");

        steps = new Texture[16];
        for (int i = 0; i < steps.length; i++) {
            steps[i] = new Texture("images/step" + (i + 1) + ".png");
        }
        butStart = new Actor();
        butExit = new Actor();
        butMenu = new Actor();
        butRestart = new Actor();
        butStart.setBounds(HALF_WIDHT - 670 / 2, HALF_HEIGHT - 140, 670, 240);
        butExit.setBounds(HALF_WIDHT - 670 / 2, HALF_HEIGHT - 500, 670, 240);
        butMenu.setBounds(HALF_WIDHT - 500, HALF_HEIGHT - 810, 480, 190);
        butRestart.setBounds(HALF_WIDHT + 20, HALF_HEIGHT - 810, 480, 190);

        butStart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setScreen(Game);
            }
        });
        butExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        butMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setScreen(Menu);
            }
        });
        butRestart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                restartGame();
            }
        });

        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
        setScreen(Menu);

        prefs = Gdx.app.getPreferences("Data 2048");
        record = prefs.getInteger("record", 0);

        score = prefs.getInteger("score", 0);

        state = new int[4][4];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                state[i][j] = prefs.getInteger("state-" + i + "-"+ j, 0);
            }
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(clBackground.r, clBackground.g, clBackground.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (screen == Menu) {
            drawText(batch, "2048", clText, 144, 0, HALF_HEIGHT + 470, WIDTH, Align.center, true);

            batch.draw(but1, butStart.getX(), butStart.getY(), butStart.getWidth(), butStart.getHeight());
            batch.draw(iconStart, butStart.getX() + 70, butStart.getY() + 77, 77, 86);
            drawText(batch, "Начать", clText, 64, butStart.getX(), butStart.getY() + 145, butStart.getWidth(), Align.center, true);
            drawPressed(butStart, but1);

            batch.draw(but1, butExit.getX(), butExit.getY(), butExit.getWidth(), butExit.getHeight());
            batch.draw(iconExit, butExit.getX() + 52, butExit.getY() + 77, 113, 86);
            drawText(batch, "Выйти", clText, 64, butExit.getX(), butExit.getY() + 145, butExit.getWidth(), Align.center, true);
            drawPressed(butExit, but1);

        } else if (screen == Game) {


            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) swipe(S_RIGHT);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) swipe(S_LEFT);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) swipe(S_UP);
            else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) swipe(S_DOWN);


            batch.draw(but2, butMenu.getX(), butMenu.getY(), butMenu.getWidth(), butMenu.getHeight());
            batch.draw(but2, butRestart.getX(), butRestart.getY(), butRestart.getWidth(), butRestart.getHeight());
            drawText(batch, "Счет: " + score + "\nРекорд: " + record, clText, 96, 0, HALF_HEIGHT + 790, WIDTH, Align.center, true);
            drawText(batch, "Меню", clText, 64, butMenu.getX(), butMenu.getY() + 118, butMenu.getWidth(), Align.center, true);
            drawText(batch, "Заново", clText, 64, butRestart.getX(), butRestart.getY() + 118, butRestart.getWidth(), Align.center, true);
            drawPressed(butMenu, but2);
            drawPressed(butRestart, but2);


            batch.draw(field, HALF_WIDHT - 500, HALF_HEIGHT - 500, 1000, 1000);

            for (int j = 0; j < state.length; j++) {
                for (int i = 0; i < state[j].length; i++) {
                    if (state[i][j] == 0) continue;
                    float x = HALF_WIDHT - 500 + 28 + 248 * j;
                    float y = HALF_HEIGHT + 500 - 228 - 248 * i;
                    batch.draw(steps[state[i][j] - 1], x, y, 200, 200);
                    String value = Integer.toString((int) Math.pow(2, state[i][j]));
                    drawText(batch, value, clText, getSize(value), x, y + 140, 200, Align.center, true);
                }
            }


            if (isEnd) {

                batch.draw(notice, HALF_WIDHT - 500, HALF_HEIGHT - 500, 1000, 1000);
                drawText(batch, "Игра окончена\nсо счетом: " + score + "\nВаш рекорд: " + record, clText, 100, HALF_WIDHT - 500, HALF_HEIGHT + 250, 1000, Align.center, true);


            }




        }

        batch.end();
    }

    private void drawPressed(Actor button, Texture texture) {
        if (((ClickListener) button.getListeners().first()).isVisualPressed()) {
            batch.setColor(0, 0, 0, 0.2f);
            batch.draw(texture, button.getX(), button.getY(), button.getWidth(), button.getHeight());
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
    }

    public static void drawText(SpriteBatch batch, String text, Color color, float size, float x, float y, float width, int align, boolean wrap) {
        font.setColor(color);
        font.getData().setScale(size / FONT_SIZE);
        font.draw(batch, text, x, y, width, align, wrap);
    }

    private void setScreen(int newScreen) {
        screen = newScreen;
        stage.clear();
        if (screen == Menu) {
            stage.addActor(butStart);
            stage.addActor(butExit);
        } else if (screen == Game) {
            startGame();
            stage.addActor(butMenu);
            stage.addActor(butRestart);
        }
    }

    private void startGame() {
        isEnd = false;
        if (isEmpty()) addStep();
    }

    private void restartGame(){
        state = new int[SIZE][SIZE];
        isEnd = false;
        score = 0;
        addStep();
        saveGame();
    }

    private void addStep() {
        if (isFull()) return;
        int i = MathUtils.random(0, 3);
        int j = MathUtils.random(0, 3);
        if (state[i][j] == 0) state[i][j] = 1;
        else addStep();
    }

    private boolean isFull() {
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                if (state[i][j] == 0) return false;
            }
        }
        return true;
    }

    private boolean isEmpty() {
        for (int i = 0; i < state.length; i++) {
            for (int j = 0; j < state[i].length; j++) {
                if (state[i][j] != 0) return false;
            }
        }
        return true;
    }



    private float getSize(String value) {
        if (value.length() == 1) return 100;
        else if (value.length() == 2) return 90;
        else if (value.length() == 3) return 72;
        else if (value.length() == 4) return 64;
        else return 48;
    }


    private void checkSwipe(int sX, int sY) {
        if (!isSwiped) {
            if (downX - sX > 100) swipe(S_LEFT);
            else if (downX - sX < -100) swipe(S_RIGHT);
            else if (downY - sY > 100) swipe(S_DOWN);
            else if (downY - sY < -100) swipe(S_UP);
        }
    }

    private void swipe(int side) {
        boolean needAddStep = false;
        if (side == S_LEFT) {

            for (int r = 0; r < SIZE; r++) {
                for (int i = 0; i < SIZE; i++) {
                    if ((i > 0 && state[r][i] == state[r][i - 1]) || state[r][i] == 0) {
                        boolean goBack = false;
                        if (state[r][i] != 0) {

                            state[r][i - 1]++;
                            score += Math.pow(2, state[r][i - 1]);
                        }
                        else goBack = true;
                        for (int j = i; j < SIZE - 1; j++) state[r][j] = state[r][j + 1];
                        state[r][SIZE - 1] = 0;
                        if (goBack || state[r][i] == 0) {

                            for (int j = i; j < SIZE; j++) {
                                if (state[r][j] > 0) {
                                    needAddStep = true;
                                    i--;
                                    break;
                                }
                            }
                        }
                    }
                }
            }


        } else if (side == S_RIGHT) {

            for (int r = 0; r < SIZE; r++) {
                for (int i = SIZE - 1; i >= 0; i--) {
                    if ((i < SIZE - 1 && state[r][i] == state[r][i + 1]) || state[r][i] == 0) {

                        boolean goBack = false;
                        if (state[r][i] != 0) {
                            state[r][i + 1]++;
                            score += Math.pow(2, state[r][i + 1]);
                        }
                        else goBack = true;
                        for (int j = i; j > 0; j--) state[r][j] = state[r][j - 1];
                        state[r][0] = 0;
                        if (goBack || state[r][i] == 0) {
                            for (int j = i; j >= 0; j--) {
                                if (state[r][j] > 0) {
                                    needAddStep = true;
                                    i++;
                                    break;
                                }
                            }
                        }
                    }

                }
            }
        } else if (side == S_DOWN) {
            for (int r = 0; r < SIZE; r++) {
                for (int i = SIZE - 1; i >= 0; i--) {
                    if ((i < SIZE - 1 && state[i][r] == state[i + 1][r]) || state[i][r] == 0) {

                        boolean goBack = false;
                        if (state[i][r] != 0) {
                            state[i + 1][r]++;
                            score += Math.pow(2, state[i + 1][r]);
                        }
                        else goBack = true;
                        for (int j = i; j > 0; j--) state[j][r] = state[j - 1][r];
                        state[0][r] = 0;
                        if (goBack || state[i][r] == 0) {
                            for (int j = i; j >= 0; j--) {
                                if (state[j][r] > 0) {
                                    needAddStep = true;
                                    i++;
                                    break;
                                }
                            }
                        }

                    }
                }
            }

        } else if (side == S_UP) {

            for (int r = 0; r < SIZE; r++) {
                for (int i = 0; i < SIZE; i++) {
                    if ((i > 0 && state[i][r] == state[i - 1][r]) || state[i][r] == 0) {

                        boolean goBack = false;
                        if (state[i][r] != 0) {
                            state[i - 1][r]++;
                            score += Math.pow(2, state[i - 1][r]);
                        }
                        else goBack = true;
                        for (int j = i; j < SIZE - 1; j++) state[j][r] = state[j + 1][r];
                        state[SIZE - 1][r] = 0;
                        if (goBack || state[i][r] == 0) {
                            for (int j = i; j < SIZE; j++) {
                                if (state[j][r] > 0) {
                                    needAddStep = true;
                                    i--;
                                    break;
                                }
                            }
                        }
                    }

                }
            }


        }

        isSwiped = true;
        if (needAddStep) addStep();
        saveGame();
        if (record < score) {
            record = score;
            prefs.putInteger("record", record);
            prefs.flush();

        }


        if (isFull()) {
            isEnd = true;
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE - 1; j++) {
                    if (state[i][j] == state[i][j + 1] || state[j][i] == state[j + 1][i]) {
                        isEnd = false;
                        break;
                    }
                }
            }
            if (isEnd) {
                for (int i = 0; i < SIZE; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        prefs.remove("state-" + i + "-" + j);


                    }
                }
                prefs.flush();
            }
        }

    }

    private void saveGame(){
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                prefs.putInteger("state-" + i + "-"+ j, state[i][j]);
            }
        }
        prefs.putInteger("score", score);
        prefs.flush();
    }


    @Override
    public boolean touchDown(int sX, int sY, int pointer, int button) {
        if (screen != Game) return false;
        sX *= AR;
        sY = (int) (HEIGHT - sY * AR);
        downX = sX;
        downY = sY;
        isSwiped = false;
        return false;
    }

    @Override
    public boolean touchDragged(int sX, int sY, int pointer) {
        if (screen != Game) return false;
        sX *= AR;
        sY = (int) (HEIGHT - sY * AR);
        checkSwipe(sX, sY);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }


    public static void main(String[] args) {
//        int[] a = {2, 1, 0, 1, 1, 0};
        int[][] a = {
                {2, 1, 0, 1},
                {1, 1, 0, 1},
                {0, 0, 0, 0},
                {2, 1, 1, 1}
        };
        System.out.println(Arrays.deepToString(a));

        for (int r = 0; r < a[0].length; r++) {
            for (int i = 0; i < a.length; i++) {
                if ((i > 0 && a[i][r] == a[i - 1][r]) || a[i][r] == 0) {
                    boolean goBack = false;
                    if (a[i][r] != 0) a[i - 1][r] = a[i][r] * 2;
                    else goBack = true;
                    for (int j = i; j < a.length - 1; j++) a[j][r] = a[j + 1][r];
                    a[a.length - 1][r] = 0;
                    if (goBack || a[i][r] == 0) {
                        for (int j = i; j < a.length; j++) {
                            if (a[j][r] > 0) {
                                i--;
                                break;
                            }
                        }
                    }
                }
            }
        }

        System.out.println(Arrays.deepToString(a));
    }


}

