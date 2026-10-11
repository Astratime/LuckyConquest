package fr.astratime.lucky.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FoilShaderTest {

    @Test
    void theShineFollowsThePixelGridOfEachCardStyle() {
        assertEquals(6f, FoilShader.pixelSize(480, 672), "pleine illustration : grille 80x112");
        assertEquals(5f, FoilShader.pixelSize(190, 270), "style des cartes classiques : grille 38x54");
    }
}
