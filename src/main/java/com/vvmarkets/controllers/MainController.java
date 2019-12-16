package com.vvmarkets.controllers;

import com.jfoenix.controls.JFXMasonryPane;
import com.vvmarkets.Main;
import com.vvmarkets.components.ProductComponent;
import com.vvmarkets.core.*;
import com.vvmarkets.dao.Product;
import com.vvmarkets.dao.Seller;
import com.vvmarkets.errors.NotFound;
import com.vvmarkets.presenters.ConfirmPresenter;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.input.MouseEvent;

public class MainController implements Initializable, IController {
    private static final Logger log = LogManager.getLogger(Main.class);
    public static String sellerId = "";
    public AnchorPane mainContainer;

    @FXML
    public AnchorPane hotAccessPane;

    @FXML
    public ListView<IListContent> hotAccessListView;
    @FXML
    public ComboBox<Seller> cmbSeller;

    private String tmpBarcode = "";

    @FXML
    private JFXMasonryPane mainMasonryPane;

    @Override
    public Node getPreviousView() {
        return previousView;
    }

    @Override
    public void setPreviousView(Node previousView) {
        this.previousView = previousView;
    }

    private Node previousView;

    @FXML
    public Button mainBtnNewTab;
    @FXML
    public Button mainBtnExit;
    @FXML
    public Button btnCloseTab;
    @FXML
    public TabPane mainTabPane;
    @FXML
    public Label lblTotal;
    @FXML
    public Button btnConfirm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Tab newTab = TabUtil.NewTab();
        newTab.setOnSelectionChanged(this::selectedTabChanged);
        mainTabPane.getTabs().add(0, newTab);

        ListUtil.INSTANCE.fillMainListView(hotAccessListView);
        Seller.fillSeller(cmbSeller);

        TableUtil.changed.subscribe(aDouble -> {
            lblTotal.setText(String.valueOf(aDouble));
        });

        mainMasonryPane.getChildren().addAll(ProductComponent.getList("hello"));
    }

    public void keyPressed(@NotNull KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            TableView tableView = (TableView) mainTabPane.getSelectionModel().getSelectedItem().getContent();

            try {
                Product product = Product.getProduct(tmpBarcode);
                product.setQuantity(1);
                TableUtil.addProduct(tableView, product);
            } catch (NotFound nf) {
                DialogUtil.newWarning("Не найден", "Товар с кодом " + tmpBarcode + " не найден").show();
            } catch (Exception e) {
                Utils.logException(e, "cannot get product");
                DialogUtil.newError("Не предвиденная ошибка", e.getMessage()).show();
            }

            tmpBarcode = "";
        } else if (keyEvent.getCode().isDigitKey()) {
            tmpBarcode += keyEvent.getText();
        }
    }

    public void confirm(ActionEvent actionEvent) throws Exception {

        if (sellerId == null || sellerId.isBlank()) {
            Alert a = DialogUtil.newWarning("Продавец не выбран", "Для продолжения выберите продавца");
            a.show();
            return;
        }

        TableView tableView = (TableView) mainTabPane.getSelectionModel().getSelectedItem().getContent();
        if (tableView.getItems().size() == 0) {
            Alert a = DialogUtil.newWarning("Корзина пуста", "Для продолжения нужно добавить товар в корзину");
            a.show();
            return;
        }
        ConfirmPresenter cp = new ConfirmPresenter(tableView);
        Utils.showScreen(cp.getView(mainContainer));
    }


    public void hotAccessClicked(MouseEvent event) {
        IListContent content = hotAccessListView.getSelectionModel().getSelectedItem();
        if (hotAccessListView.getSelectionModel().getSelectedIndex() == 0 && content.getType() == ListContentType.Product) {
            ListUtil.INSTANCE.fillMainListView(hotAccessListView);
        }

        if (content == null || content.getQueryId() == null || content.getQueryId().isEmpty()) {
            return;
        }

        switch (content.getType()) {
            case Category:
                hotAccessListView.getItems().clear();
                ListUtil.INSTANCE.fillForCategory(hotAccessListView, content.getQueryId());
                break;
            case Product:
                try {
                    Product clickedRow = Product.getProduct(content.getQueryId());
                    Dialog dialog = DialogUtil.getQuantityDialog(1);
                    Optional<String> result = dialog.showAndWait();

                    if (result.isPresent()) {
                        double entered = Utils.getDoubleOrZero(result.get());

                        if (entered > 0) {
                            clickedRow.setQuantity(entered);
                            TableView tableView = (TableView) mainTabPane.getSelectionModel().getSelectedItem().getContent();
                            TableUtil.addProduct(tableView, clickedRow);
                        } else {
                            DialogUtil.newError("Неправильное число", "Пожалуйста введите правильное число").show();
                        }
                    }
                } catch (NotFound nf) {
                    DialogUtil.newWarning("Не найден", "Товар с кодом " + tmpBarcode + " не найден").show();
                } catch (Exception e) {
                    DialogUtil.newError("Не предвиденная ошибка", e.getMessage()).show();
                }
                break;
            default:
                break;
        }
    }

    public void newTabClicked(MouseEvent mouseEvent) {
        Tab newTab = TabUtil.NewTab();
        newTab.setOnSelectionChanged(this::selectedTabChanged);
        mainTabPane.getTabs().add(newTab);
        mainTabPane.getSelectionModel().select(newTab);
    }

    private void selectedTabChanged(Event event) {
        TableView tableView = (TableView) mainTabPane.getSelectionModel().getSelectedItem().getContent();
        lblTotal.setText(String.valueOf(TableUtil.calculateTotal(tableView)));
    }

    public void sellerChanged(ActionEvent actionEvent) {
        sellerId = cmbSeller.getSelectionModel().getSelectedItem().getId();
    }

    public void closeTabClicked(MouseEvent mouseEvent) {
        mainTabPane.getTabs().remove(mainTabPane.getSelectionModel().getSelectedIndex());
    }

    public void containerClicked(MouseEvent mouseEvent) {
        ProductComponent pc = null;
        if(mouseEvent.getPickResult().getIntersectedNode() instanceof ProductComponent) {
            pc = (ProductComponent)mouseEvent.getPickResult().getIntersectedNode();
        } else if(mouseEvent.getPickResult().getIntersectedNode() instanceof ImageView) {
            pc = (ProductComponent)mouseEvent.getPickResult().getIntersectedNode().getParent();
        }

        if (pc != null) {
            System.out.println(pc.getProduct().getId());
        }
    }

    public void logout(ActionEvent actionEvent) {
        mainMasonryPane.getChildren().clear();
    }
}
