package br.edu.utfpr.cp.espjava.crudcidades.cidade;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CidadeController {

    private Set<Cidade> cidades;
    private final CidadeRepository repository;

    public CidadeController(CidadeRepository repository) {
        cidades = new HashSet<>();
        this.repository = repository;
    }

    @GetMapping("/")
    public String listar(Model memoria, 
            Principal usuario, 
            HttpSession sessao, 
            HttpServletResponse reponse) {
        reponse.addCookie(new Cookie("listar", LocalDateTime.now().toString()));

        memoria.addAttribute("listaCidades", repository
                .findAll()
                .stream()
                .map(Cidade::clonar)
                .collect(Collectors.toList()));
        
        sessao.setAttribute("usuarioAtual", usuario.getName());

        return "/crud";
    }

    @PostMapping("/criar")
    public String criar(@Valid Cidade cidade, BindingResult validacao, Model memoria, HttpServletResponse reponse) {

        reponse.addCookie(new Cookie("criar", LocalDateTime.now().toString()));
        if (validacao.hasErrors()) {
            validacao
                    .getFieldErrors()
                    .forEach(error
                            -> memoria.addAttribute(
                            error.getField(),
                            error.getDefaultMessage())
                    );

            memoria.addAttribute("nomeInformado", cidade.getNome());
            memoria.addAttribute("estadoInformado", cidade.getEstado());
            memoria.addAttribute("listaCidades", cidades);

            return ("/crud");
        } else {

            repository.save(cidade.clonar());
        }

        return "redirect:/";
    }

    @GetMapping("/excluir")
    public String excluir(
            @RequestParam String nome,
            @RequestParam String estado,
            HttpServletResponse reponse) {
        reponse.addCookie(new Cookie("excluir", LocalDateTime.now().toString()));

        var cidadeEstadoEncontrada = repository.findByNomeAndEstado(nome, estado);
        cidadeEstadoEncontrada.ifPresent(repository::delete);
        return "redirect:/";
    }

    @GetMapping("/preparaAlterar")
    public String preparaAlterar(
            @RequestParam String nome,
            @RequestParam String estado,
            Model memoria) {

        var cidadeAtual = repository.findByNomeAndEstado(nome, estado);

        cidadeAtual.ifPresent(cidadeEncontrada -> {
            memoria.addAttribute("cidadeAtual", cidadeEncontrada);
            memoria.addAttribute("listaCidades", repository.findAll());
        });

        return "/crud";
    }

    @PostMapping("/alterar")
    public String alterar(
            @RequestParam String nomeAtual,
            @RequestParam String estadoAtual,
            Cidade cidade,
            HttpServletResponse reponse) {
        
        reponse.addCookie(new Cookie("alterar", LocalDateTime.now().toString()));
        var cidadeAtual = repository.findByNomeAndEstado(nomeAtual, estadoAtual);
        
        if (cidadeAtual.isPresent()) {
            var cidadeEncontrada = cidadeAtual.get();
            cidadeEncontrada.setNome(nomeAtual);
            cidadeEncontrada.setEstado(estadoAtual);

            repository.saveAndFlush(cidadeEncontrada);

        }

        return "redirect:/";
    }
    @GetMapping("/mostrar")
    @ResponseBody
    public String mostrarCookieAlterar(@CookieValue String listar){
        return "??ltimo acesso ao m??tod listar" + listar;
    }
}
