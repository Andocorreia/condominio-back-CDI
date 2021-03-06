package br.com.livraria.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.livraria.bean.entity.AutorEntity;
import br.com.livraria.bean.entity.EditoraEntity;
import br.com.livraria.bean.entity.LivroEntity;
import br.com.livraria.dao.AutorDao;
import br.com.livraria.dao.EditoraDao;
import br.com.livraria.dao.LivroDao;
import br.com.livraria.model.AutorModel;
import br.com.livraria.model.EditoraModel;
import br.com.livraria.model.LivroModel;

@Named
@ViewScoped
public class LivroBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private LivroEntity livroEntity = new LivroEntity();
	private Integer autorId;
	private Integer editoraId;
	private DateTimeFormatter dateFormater = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	@Inject
	private LivroDao livroDao;

	@Inject
	private AutorDao autorDao;

	@Inject
	private EditoraDao editoraDao;

	public void gravar() {
		if (!validacao()) {
			final LivroModel model = new LivroModel();

			livroEntity.getAutor().stream().forEach(autor -> {
				model.setAutor(autorDao.buscaPorId(AutorModel.class, autor.getCodigo()));
			});
			model.setEditora(getEditoraModel());
			model.setDataLancamento(livroEntity.getDataLancamento());
			model.setPaginas(livroEntity.getPaginas());
			model.setSummary(livroEntity.getSummary());
			model.setTitulo(livroEntity.getTitulo());
			model.setValorUnitario(livroEntity.getValorUnitario());

			if (this.livroEntity.getCodigo() == null) {
				livroDao.adiciona(model);
			}
			else {
				model.setCodigo(this.livroEntity.getCodigo());
				livroDao.atualiza(model);
			}
			livroEntity = new LivroEntity();
		}
	}

	public void editarLivro(final LivroModel livro) {
		this.livroEntity = new LivroEntity();
		this.livroEntity.setCodigo(livro.getCodigo());
		this.livroEntity.setSummary(livro.getSummary());
		this.livroEntity.setTitulo(livro.getTitulo());
		this.livroEntity.setValorUnitario(livro.getValorUnitario());
		this.livroEntity.setPaginas(livro.getPaginas());
		this.livroEntity.setDataLancamento(livro.getDataLancamento());

		livro.getAutor().stream().forEach(autor -> {
			AutorEntity entity = new AutorEntity();
			entity.setCodigo(autor.getCodigo());
			entity.setNome(autor.getNome());
			this.livroEntity.setAutor(entity);
		});

		this.editoraId = livro.getEditora().getCodigo();
		EditoraEntity editoraEntity = new EditoraEntity();
		editoraEntity.setCodigo(livro.getEditora().getCodigo());
		editoraEntity.setNome(livro.getEditora().getNome());
		this.livroEntity.setEditora(editoraEntity);
	}

	public void selecionarAutor() {
		if (this.livroEntity.getAutor().stream().filter(entity -> entity.getCodigo() == this.autorId).count() == 0) {
			AutorModel model = autorDao.buscaPorId(AutorModel.class, this.autorId);
			AutorEntity entity = new AutorEntity();
			entity.setCodigo(model.getCodigo());
			entity.setNome(model.getNome());
			this.livroEntity.setAutor(entity);
		}
	}

	private EditoraModel getEditoraModel() {
		return editoraDao.buscaPorId(EditoraModel.class, this.editoraId);
	}

	public void removerLivro(final LivroModel livro) {
		livroDao.remove(livro.getCodigo());
	}

	public void setAutorId(final Integer id) {
		this.autorId = (id);
	}

	public Integer getAutorId() {
		return autorId;
	}

	public void setEditoraId(final Integer id) {
		this.editoraId = id;
	}

	public Integer getEditoraId() {
		return editoraId;
	}

	public LivroEntity getLivroEntity() {
		return livroEntity;
	}

	public void removerAutor(final AutorEntity autor) {
		this.livroEntity.getAutor().removeIf(entity -> entity.getCodigo().equals(autor.getCodigo()));

	}

	public Collection<AutorEntity> getListaAutores() {
		Collection<AutorEntity> autorEntity = new ArrayList<>();
		autorDao.listaTodos(AutorModel.class).stream().forEach(autor -> {
			AutorEntity entity = new AutorEntity();
			entity.setCodigo(autor.getCodigo());
			entity.setNome(autor.getNome());
			autorEntity.add(entity);
		});
		return autorEntity;
	}

	public Collection<EditoraEntity> getListaEditoras() {
		Collection<EditoraEntity> editoraEntity = new ArrayList<>();

		editoraDao.listaTodos(EditoraModel.class).stream().forEach(editora -> {
			EditoraEntity entity = new EditoraEntity();
			entity.setCodigo(editora.getCodigo());
			entity.setNome(editora.getNome());
			editoraEntity.add(entity);
		});
		return editoraEntity;
	}

	public Collection<LivroModel> getListaLivros() {
		return livroDao.listaTodos(LivroModel.class);
	}

	private LocalDate convertStringToDate(final String data) {
		return LocalDate.parse(data, dateFormater);
	}

	private boolean validacao() {
		Boolean erro = false;
		if (this.livroEntity.getTitulo().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erro-Campo Titulo Obrigatório"));
			erro = true;
		}
		if (this.livroEntity.getSummary().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erro-Campo Resumo Obrigatório"));
			erro = true;
		}
		if (this.livroEntity.getDataLancamento().toString().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erro-Campo Data de Lançamento Obrigatório"));
			erro = true;
		}
		if (this.livroEntity.getPaginas() == null || this.livroEntity.getPaginas() == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erro-Campo Paginas Obrigatório"));
			erro = true;
		}
		if (this.livroEntity.getValorUnitario() == null || this.livroEntity.getValorUnitario().compareTo(BigDecimal.ZERO) == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erro-Campo Valor Obrigatório"));
			erro = true;
		}
		return erro;
	}
}
