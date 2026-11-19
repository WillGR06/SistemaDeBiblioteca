package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.util.ArmazenamentoCapas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmazenamentoCapasUnitTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    private static final byte[] GIF = {'G', 'I', 'F', '8', '9', 'a', 0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    @TempDir
    Path temp;

    private ArmazenamentoCapas armazenamento;

    @BeforeEach
    void criar() {
        armazenamento = new ArmazenamentoCapas(temp.toString());
    }

    private String enviar(byte[] conteudo) {
        return armazenamento.salvar(new MockMultipartFile("capa", "qualquer-nome.exe", "image/png", conteudo));
    }

    @Test
    void aceitaPngJpgGifEWebpPeloConteudoEGeraNomeProprio() {
        assertTrue(enviar(PNG).matches("/capas/[0-9a-f-]{36}\\.png"));
        assertTrue(enviar(JPG).endsWith(".jpg"));
        assertTrue(enviar(GIF).endsWith(".gif"));
        assertTrue(enviar(WEBP).endsWith(".webp"));
    }

    @Test
    void gravaOArquivoNaPastaDeCapas() {
        String url = enviar(PNG);

        assertTrue(Files.exists(armazenamento.pasta().resolve(url.substring("/capas/".length()))));
    }

    @Test
    void recusaArquivoQueNaoEhImagem() {
        byte[] texto = "<svg onload=alert(1)>".getBytes();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> enviar(texto));

        assertEquals("Envie uma imagem JPG, PNG, GIF ou WEBP.", e.getMessage());
    }

    @Test
    void recusaArquivoCurtoDemais() {
        assertThrows(IllegalArgumentException.class, () -> enviar(new byte[]{1}));
    }

    @Test
    void recusaImagemMaiorQueCincoMegabytes() {
        byte[] grande = new byte[(int) ArmazenamentoCapas.TAMANHO_MAXIMO + 1];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> enviar(grande));

        assertEquals("A imagem é muito grande. O limite é de 5 MB.", e.getMessage());
    }

    @Test
    void apagarRemoveAImagemGuardada() {
        String url = enviar(PNG);
        Path arquivo = armazenamento.pasta().resolve(url.substring("/capas/".length()));

        armazenamento.apagar(url);

        assertFalse(Files.exists(arquivo));
    }

    @Test
    void apagarIgnoraNuloEnderecoExternoEFugaDePasta() throws IOException {
        Path fora = Files.writeString(temp.resolve("segredo.txt"), "x");

        armazenamento.apagar(null);
        armazenamento.apagar("https://exemplo.com/capa.png");
        armazenamento.apagar("/capas/../segredo.txt");

        assertTrue(Files.exists(fora));
    }
}
