package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.util.ArmazenamentoCapas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Casos de borda do armazenamento de capas: assinaturas incompletas, falhas de disco e pastas. */
class ArmazenamentoCapasRamosUnitTest {

    @TempDir
    Path temp;

    private static byte[] b(int... valores) {
        byte[] r = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            r[i] = (byte) valores[i];
        }
        return r;
    }

    @Test
    void recusaAssinaturasIncompletasOuErradas() {
        ArmazenamentoCapas armazenamento = new ArmazenamentoCapas(temp.toString());
        List<byte[]> invalidos = List.of(
                new byte[0],
                b(0xFF, 0xD8),                                    // JPEG curto demais
                b(0xFF, 0xD8, 0x00),                              // JPEG com o 3º byte errado
                b(0xFF, 0x00, 0xFF),                              // JPEG com o 2º byte errado
                b(0x00, 0xD8, 0xFF),                              // JPEG com o 1º byte errado
                b(0x89, 'P', 'N', 'G'),                           // PNG curto demais
                b(0x89, 'P', 'N', 'X', 0, 0, 0, 0),               // PNG com o 4º byte errado
                b(0x89, 'P', 'X', 'G', 0, 0, 0, 0),               // PNG com o 3º byte errado
                b(0x89, 'X', 'N', 'G', 0, 0, 0, 0),               // PNG com o 2º byte errado
                b(0x00, 'P', 'N', 'G', 0, 0, 0, 0),               // PNG com o 1º byte errado
                b('G', 'I', 'F', '8'),                            // GIF curto demais
                b('G', 'I', 'F', 'X', 0, 0),                      // GIF com o 4º byte errado
                b('G', 'I', 'X', '8', 0, 0),                      // GIF com o 3º byte errado
                b('G', 'X', 'F', '8', 0, 0),                      // GIF com o 2º byte errado
                b('X', 'I', 'F', '8', 0, 0),                      // GIF com o 1º byte errado
                b('R', 'I', 'F', 'F'),                            // WEBP curto demais
                b('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'X'),
                b('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'X', 'P'),
                b('R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'X', 'B', 'P'),
                b('R', 'I', 'F', 'F', 0, 0, 0, 0, 'X', 'E', 'B', 'P'),
                b('R', 'I', 'F', 'X', 0, 0, 0, 0, 'W', 'E', 'B', 'P'),
                b('R', 'I', 'X', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'),
                b('R', 'X', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'),
                b('X', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'));

        for (byte[] conteudo : invalidos) {
            MockMultipartFile arquivo = new MockMultipartFile("capa", "x.png", "image/png", conteudo);
            assertThrows(IllegalArgumentException.class, () -> armazenamento.salvar(arquivo));
        }
    }

    @Test
    void imagemNoLimiteExatoDeCincoMegabytesEhAceita() {
        ArmazenamentoCapas armazenamento = new ArmazenamentoCapas(temp.toString());
        byte[] limite = new byte[(int) ArmazenamentoCapas.TAMANHO_MAXIMO];
        byte[] png = b(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
        System.arraycopy(png, 0, limite, 0, png.length);

        String url = armazenamento.salvar(new MockMultipartFile("capa", "x.png", "image/png", limite));

        assertTrue(url.endsWith(".png"));
    }

    @Test
    void falhaDeLeituraDoArquivoViraErroDeEntradaESaida() throws IOException {
        ArmazenamentoCapas armazenamento = new ArmazenamentoCapas(temp.toString());
        MultipartFile quebrado = mock(MultipartFile.class);
        when(quebrado.getSize()).thenReturn(10L);
        when(quebrado.getBytes()).thenThrow(new IOException("disco"));

        assertThrows(UncheckedIOException.class, () -> armazenamento.salvar(quebrado));
    }

    @Test
    void naoCriaAPastaQuandoOCaminhoJaEhUmArquivo() throws IOException {
        Path arquivo = Files.writeString(temp.resolve("ocupado.txt"), "x");

        assertThrows(UncheckedIOException.class, () -> new ArmazenamentoCapas(arquivo.toString()));
    }

    @Test
    void apagarNaoQuebraQuandoOAlvoEhUmaPastaCheia() throws IOException {
        ArmazenamentoCapas armazenamento = new ArmazenamentoCapas(temp.toString());
        Path pastaCheia = Files.createDirectories(armazenamento.pasta().resolve("pasta"));
        Files.writeString(pastaCheia.resolve("dentro.txt"), "x");

        armazenamento.apagar("/capas/pasta");

        assertTrue(Files.exists(pastaCheia.resolve("dentro.txt")));
    }

    @Test
    void apagarArquivoInexistenteNaoFazNada() {
        ArmazenamentoCapas armazenamento = new ArmazenamentoCapas(temp.toString());

        armazenamento.apagar("/capas/nao-existe.png");

        assertEquals(0, armazenamento.pasta().toFile().list().length);
    }
}
