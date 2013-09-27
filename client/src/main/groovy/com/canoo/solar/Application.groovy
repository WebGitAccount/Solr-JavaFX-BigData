package com.canoo.solar

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import javafx.util.Callback
import javafx.util.Duration
import np.com.ngopal.control.AutoFillTextBox
import org.opendolphin.core.Attribute;
import org.opendolphin.core.PresentationModel;
import org.opendolphin.core.client.ClientAttribute;
import org.opendolphin.core.client.ClientDolphin;
import org.opendolphin.core.client.ClientPresentationModel
import org.opendolphin.core.client.comm.WithPresentationModelHandler
import java.beans.PropertyChangeListener
import static com.canoo.solar.Constants.CMD.GET_CITIES
import static com.canoo.solar.Constants.CMD.GET_TYPE
import static com.canoo.solar.Constants.FilterConstants.CITY;
import static com.canoo.solar.Constants.FilterConstants.FILTER
import static com.canoo.solar.Constants.FilterConstants.ID
import static com.canoo.solar.Constants.FilterConstants.NOMINAL_POWER
import static com.canoo.solar.Constants.FilterConstants.PLANT_TYPE;
import static com.canoo.solar.Constants.FilterConstants.*
import static com.canoo.solar.Constants.CMD.GET
import static com.canoo.solar.Constants.FilterConstants.ZIP;
import static org.opendolphin.binding.JFXBinder.bind
import static com.canoo.solar.ApplicationConstants.*

public class Application extends javafx.application.Application {
    static ClientDolphin clientDolphin;
    javafx.collections.ObservableList<Integer> observableList = FXCollections.observableArrayList()
    javafx.collections.ObservableList<Integer> observableListCities = FXCollections.observableArrayList()
    javafx.collections.ObservableList<Integer> observableListTypes = FXCollections.observableArrayList()

    private PresentationModel textAttributeModel;
    private TableView table = new TableView();
    Label cityLabelforDetail = new Label("City:       ")
    Label zipLabelforDetail = new Label("Zip Code:")
    Label typeLabelforDetail = new Label("Type:      ")
    Label nominalLabelforDetail = new Label("Power:    ")
    Label idLabelforDetail = new Label("Id:          ")
    TextField cityLabelDetail = new TextField("City")
    TextField idLabelDetail = new TextField("Id")
    TextField zipLabelDetail = new TextField("Zip Code")
    TextField typeLabelDetail = new TextField("Type")
    TextField nominalLabelDetail = new TextField("Power")
    AutoFillTextBox typeChoiceBox = new AutoFillTextBox(observableListTypes)
    AutoFillTextBox cityText = new AutoFillTextBox(observableListCities)
    TableColumn idCol = new TableColumn("Position");
    TableColumn typeCol = new TableColumn("Plant Type");
    TableColumn cityCol = new TableColumn("City");
    TableColumn zipCol = new TableColumn("ZIP Code");
    TableColumn nominalCol = new TableColumn("Nominal Power");
    Label loading = new Label("Loading Data from Solr")
    Label noData = new Label("No Data Found")
    Label search = new Label("Selection Details \n")
    Label columns = new Label("Columns")
    Label filter = new Label("Filters")
    private Rectangle2D boxBounds = new Rectangle2D(600, 100, 650, 200);
    int f, c = 0
    CheckBox cityCB = new CheckBox("Show Cities");
    CheckBox typeCB = new CheckBox("Show Types");
    CheckBox cityFilterCB = new CheckBox("Filter by City");
    CheckBox typeFilterCB = new CheckBox("Filter by Type");
    CheckBox zipCB = new CheckBox("Show Zip-Codes");
    CheckBox nominalCB = new CheckBox("Show Nominal Powers");
    final TreeItem<String> allItem = new TreeItem<String>("All");
    Pane columnStack = new Pane()
    Pane filterStack = new Pane()
    Pane tableStack = new Pane()
    Pane pane = new Pane()
    private Rectangle clipRect;
    private Timeline timelineLeft;
    private Timeline timelineRight;
    private Timeline timelineLeftFilters;
    private Timeline timelineRightFilters;
    final TreeView treeCities = new TreeView();
    final TreeView treeTypes = new TreeView();
    TextField zipText = new TextField()
    final Label selectionLabeltypes = new Label();
    final Label selectionLabelcities = new Label();
    final Separator separator = new Separator();
    final Separator separator2 = new Separator();
    TextField nominalText = new TextField()
    TextField typeLabelforBinding = typeChoiceBox.getTextbox()
    TextField cityLabelforBinding = cityText.getTextbox()


    public Application() {
        textAttributeModel = clientDolphin.presentationModel(PM_APP, new ClientAttribute(ATT_ATTR_ID, null));
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setTitle("Application Title");
        initializePresentationModels();

        clientDolphin.data GET_CITIES, { data ->
            observableListCities.addAll( data.get(0).get("ids")  )
        }

        clientDolphin.data GET_TYPE, { data ->
            observableListTypes.addAll( data.get(0).get("ids")  )
        }
        Pane root = setupStage();
        setupBinding();

        Scene scene = new Scene(root, 700, 640)
        scene.stylesheets << 'demo.css'

        stage.setScene(scene);
        stage.setTitle(getClass().getName());
        stage.show();
    }

    private static void initializePresentationModels () {
        clientDolphin.presentationModel(FILTER, [ID, CITY, PLANT_TYPE, ZIP, NOMINAL_POWER]);
        clientDolphin.presentationModel(SELECTED_POWERPLANT, [ID, CITY, PLANT_TYPE, ZIP, NOMINAL_POWER]);
        clientDolphin.presentationModel(STATE, [TRIGGER])[TRIGGER].value=0


    }

    private Pane setupStage() {
        loading.setScaleX(1.2)
        loading.setScaleY(1.2)
        loading.setTextFill(Color.BLUE)

        typeLabelDetail.setEditable(false)
        cityLabelDetail.setEditable(false)
        zipLabelDetail.setEditable(false)
        nominalLabelDetail.setEditable(false)
        idLabelDetail.setEditable(false)

        cityCB.setSelected(true);
        typeCB.setSelected(true);
        nominalCB.setSelected(true);
        zipCB.setSelected(true);

        Rectangle columnCBBorder = createCBsBorder()
        Rectangle columnEventBorder = createEventBorder()
        columnEventBorder.relocate(0, 290)
        Rectangle filtersCBBorder = createCBsBorder()
        Rectangle filtersEventBorder = createEventBorder()
        filtersEventBorder.relocate(0, 420)

        table.setMinHeight(700)
        table.getColumns().addAll(idCol, typeCol, cityCol, zipCol, nominalCol);
        table.items = observableList
        table.setPlaceholder(loading)
        table.selectionModel.selectedItemProperty().addListener( { o, oldVal, selectedPm ->
            if (selectedPm==null) return;
            clientDolphin.clientModelStore.withPresentationModel(selectedPm.toString(), new WithPresentationModelHandler() {
                void onFinished(ClientPresentationModel presentationModel) {
                    clientDolphin.apply presentationModel to clientDolphin[SELECTED_POWERPLANT]

                }
            } )
        } as ChangeListener )

        idCol.cellValueFactory = {
            String lazyId = it.value
            def placeholder = new SimpleStringProperty("Not Loaded");
            clientDolphin.clientModelStore.withPresentationModel(lazyId, new WithPresentationModelHandler() {
                void onFinished(ClientPresentationModel presentationModel) {
                    placeholder.setValue( presentationModel.getAt("position").value.toString() ) // fill async lazily
                }
            } )
            return placeholder
        } as Callback
        typeCol.cellValueFactory = {
            String lazyId = it.value
            def placeholder = new SimpleStringProperty("Not Loaded")
            clientDolphin.clientModelStore.withPresentationModel(lazyId, new WithPresentationModelHandler() {
                void onFinished(ClientPresentationModel presentationModel) {
                    placeholder.setValue( presentationModel.getAt(PLANT_TYPE).value.toString() ) // fill async lazily
                }
            } )
            return placeholder
        } as Callback
        cityCol.cellValueFactory = {
            String lazyId = it.value
            def placeholder = new SimpleStringProperty("Not Loaded")
            clientDolphin.clientModelStore.withPresentationModel(lazyId, new WithPresentationModelHandler() {
                void onFinished(ClientPresentationModel presentationModel) {
                    placeholder.setValue( presentationModel.getAt(CITY).value.toString() ) // fill async lazily
                }
            } )
            return placeholder
        } as Callback
        zipCol.cellValueFactory = {
            String lazyId = it.value
            def placeholder = new SimpleStringProperty("Not Loaded")
            clientDolphin.clientModelStore.withPresentationModel(lazyId, new WithPresentationModelHandler() {
                void onFinished(ClientPresentationModel presentationModel) {
                    placeholder.setValue( presentationModel.getAt(ZIP).value.toString() ) // fill async lazily
                }
            } )
            return placeholder
        } as Callback
        nominalCol.cellValueFactory = {
            String lazyId = it.value
            def placeholder = new SimpleStringProperty("Not Loaded")
            clientDolphin.clientModelStore.withPresentationModel(lazyId, new WithPresentationModelHandler() {
                void onFinished(ClientPresentationModel presentationModel) {
                    placeholder.setValue(presentationModel.getAt(NOMINAL_POWER).value.toString()) // fill async lazily
                }
            } )
            return placeholder
        } as Callback

        ChoiceBoxListener(cityCB, table, cityCol)
        ChoiceBoxListener(typeCB, table, typeCol)
        ChoiceBoxListener(nominalCB, table, nominalCol)
        ChoiceBoxListener(zipCB, table, zipCol)

        HBox nominalLabelTextDetail = createPair(nominalLabelDetail, nominalLabelforDetail)
        HBox typeLabelTextDetail = createPair(typeLabelDetail, typeLabelforDetail)
        HBox cityLabelTextDetail = createPair(cityLabelDetail, cityLabelforDetail)
        HBox zipLabelTextDetail = createPair(zipLabelDetail, zipLabelforDetail)
        HBox idLabelTextDetail = createPair(idLabelDetail, idLabelforDetail)

        VBox columnsCBs = new VBox()
        columnsCBs.setPadding(new Insets(20, 0, 0, 10));
        columnsCBs.getChildren().addAll(cityCB, typeCB, zipCB, nominalCB)

        VBox filtersCBs = new VBox()
        filtersCBs.setPadding(new Insets(30, 0, 0, 10));
        filtersCBs.getChildren().addAll(cityFilterCB, typeFilterCB)

        TreeItem<String> rootItem =
            new TreeItem<String>("Plant Types");
        rootItem.setExpanded(true);
        rootItem.getChildren().add(allItem)
        treeTypes.setRoot(rootItem);
        treeTypes.setShowRoot(true);
        treeTypes.setMaxHeight(200)

        TreeItem<String> rootItemCities =
            new TreeItem<String>("Cities");
        rootItemCities.setExpanded(true);
        rootItemCities.getChildren().add(allItem)
        treeCities.setRoot(rootItemCities);
        treeCities.setShowRoot(true);
        treeCities.setMaxHeight(200)

        VBox trees = new VBox()
        trees.setSpacing(5)
        separator.setMinWidth(trees.getTranslateX())
        separator2.setMinWidth(trees.getTranslateX())

        VBox details = new VBox()
        details.setSpacing(5);
        details.setPadding(new Insets(20, 0, 0, 10));
        details.getChildren().addAll(search, cityLabelTextDetail, idLabelTextDetail, typeLabelTextDetail, nominalLabelTextDetail, zipLabelTextDetail, separator, trees/*, separator2, autofillHbox*/)

        typeFilterCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                def size = observableListTypes.size()
                if (f==0 && size>1){ f++
                        observableListTypes.each {
                        final TreeItem<String> checkBoxTreeItem =
                            new TreeItem<String>(it.toString());
                        rootItem.getChildren().add(checkBoxTreeItem);
                        }
                }
                if (newValue) {
                    trees.getChildren().add(treeTypes)
                }
                else trees.getChildren().remove(treeTypes)
            }
        });

        cityFilterCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            def size = observableListCities.size()
                if (c==0 && size>1){
                    c++
                    observableListCities.each {
                    final TreeItem<String> checkBoxTreeItem =
                        new TreeItem<String>(it.toString());
                    rootItemCities.getChildren().add(checkBoxTreeItem);
                    }
                }
                if (newValue) {
                    trees.getChildren().add(treeCities)
                }
                else trees.getChildren().remove(treeCities)
                            }
        });

        treeTypes.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<TreeItem <String>>() {
                    public void changed(ObservableValue<? extends TreeItem<String>> observableValue,
                                        TreeItem<String> oldItem, TreeItem<String> newItem) {
                        selectionLabeltypes.setText(newItem.getValue());
                    }
                });
        treeCities.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<TreeItem <String>>() {
                    public void changed(ObservableValue<? extends TreeItem<String>> observableValue,
                                        TreeItem<String> oldItem, TreeItem<String> newItem) {
                        selectionLabelcities.setText(newItem.getValue());
                    }
                });

        setAnimation();
        setAnimationFilters();
        setMouseEventSliding(columnEventBorder, columnStack, timelineRight, timelineLeft, columns)
        setMouseEventSliding(filtersEventBorder, filterStack, timelineRightFilters, timelineLeftFilters, filter)


        Pane all = new Pane();
        all.getChildren().addAll(details)

        columnStack.getChildren().addAll( columnCBBorder, columnsCBs)
        columnStack.relocate(0, 290)

        filterStack.getChildren().addAll( filtersCBBorder, filtersCBs)
        filterStack.relocate(0, 420)
        columns.relocate(5,340)
        columns.setRotate(90)
        filter.relocate(13,470)
        filter.setRotate(90)
        tableStack.getChildren().addAll(table,columns,filter, columnEventBorder, filtersEventBorder, columnStack, filterStack)

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(tableStack);
        borderPane.setRight(all);

        pane.getChildren().addAll(borderPane)

        return pane

    }

    /*METHODS*/
    static private Rectangle createEventBorder(){

        Rectangle eventBorder = new Rectangle()
        eventBorder.setStroke(Color.INDIANRED)
        eventBorder.setStrokeWidth(2)
        eventBorder.setWidth(40)
        eventBorder.setHeight(100)
        eventBorder.setFill(Color.WHITESMOKE)
        eventBorder.setArcWidth(10)
        eventBorder.setArcHeight(10)
        eventBorder.setOpacity(0.3)
        return eventBorder
    }

     static private Rectangle createCBsBorder(){
        Rectangle border = new Rectangle()
         border.setStroke(Color.INDIANRED)
         border.setStrokeWidth(2)
         border.setWidth(170)
         border.setHeight(100)
         border.setFill(Color.WHITESMOKE)
         border.setArcWidth(10)
         border.setArcHeight(10)
         border.setOpacity(0.8)
        return border
    }

    static private void setMouseEventSliding(Rectangle border, Pane pane, Timeline show, Timeline hide, Label tooltip){

        border.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                border.setVisible(false)
                border.setDisable(true)
                show.play();
                tooltip.setVisible(false)


            }
        });

        pane.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                border.setVisible(true)
                border.setDisable(false)
                hide.play();
                tooltip.setVisible(true)

            }
        });
    }

    static private HBox createPair(TextField detail, Label detailTooltip){

        HBox hBox = new HBox()
        hBox.setSpacing(5);
        hBox.setPadding(new Insets(10, 0, 0, 10));
        hBox.getChildren().addAll(detailTooltip, detail)
        return hBox
    }

    private void setAnimation(){
        // Initially hiding the  Pane
        clipRect = new Rectangle();
        clipRect.setWidth(30);
        clipRect.setHeight(boxBounds.getHeight());
        clipRect.translateXProperty().set(boxBounds.getWidth());
        columnStack.setClip(clipRect);
        columnStack.translateXProperty().set(-boxBounds.getWidth());

        // Animation for bouncing effect.
        final Timeline timelineBounce = new Timeline();
        timelineBounce.setCycleCount(2);
        timelineBounce.setAutoReverse(true);
        final KeyValue kv1 = new KeyValue(clipRect.widthProperty(), (boxBounds.getWidth()-15));
        final KeyValue kv2 = new KeyValue(clipRect.translateXProperty(), 15);
        final KeyValue kv3 = new KeyValue(columnStack.translateXProperty(), -15);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(100), kv1, kv2, kv3);
        timelineBounce.getKeyFrames().add(kf1);

        // Event handler to call bouncing effect after the scroll down is finished.
        EventHandler<ActionEvent> onFinished = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                timelineBounce.play();
            }
        };

        timelineRight = new Timeline();
        timelineLeft = new Timeline();

        // Animation for scroll down.
        timelineRight.setCycleCount(1);
        timelineRight.setAutoReverse(true);
        final KeyValue kvDwn1 = new KeyValue(clipRect.widthProperty(), boxBounds.getWidth());
        final KeyValue kvDwn2 = new KeyValue(clipRect.translateXProperty(), 0);
        final KeyValue kvDwn3 = new KeyValue(columnStack.translateXProperty(), 0);
        final KeyFrame kfDwn = new KeyFrame(Duration.millis(200), onFinished, kvDwn1, kvDwn2, kvDwn3);
        timelineRight.getKeyFrames().add(kfDwn);

        // Animation for scroll up.
        timelineLeft.setCycleCount(1);
        timelineLeft.setAutoReverse(true);
        final KeyValue kvUp1 = new KeyValue(clipRect.widthProperty(), 0);
        final KeyValue kvUp2 = new KeyValue(clipRect.translateXProperty(), boxBounds.getWidth());
        final KeyValue kvUp3 = new KeyValue(columnStack.translateXProperty(), -boxBounds.getWidth());
        final KeyFrame kfUp = new KeyFrame(Duration.millis(200), kvUp1, kvUp2, kvUp3);
        timelineLeft.getKeyFrames().add(kfUp);
    }

    private void setupBinding() {
        bind 'text' of zipText to ZIP of clientDolphin[FILTER]
        bind 'text' of selectionLabelcities to CITY of clientDolphin[FILTER]
        bind 'text' of selectionLabeltypes to PLANT_TYPE of clientDolphin[FILTER]
        bind 'text' of nominalText to NOMINAL_POWER of clientDolphin[FILTER]

        bind ZIP of clientDolphin[SELECTED_POWERPLANT] to 'text' of zipLabelDetail
        bind CITY of clientDolphin[SELECTED_POWERPLANT] to 'text' of cityLabelDetail
        bind ID of clientDolphin[SELECTED_POWERPLANT] to 'text' of idLabelDetail
        bind NOMINAL_POWER of clientDolphin[SELECTED_POWERPLANT] to 'text' of nominalLabelDetail
        bind PLANT_TYPE of clientDolphin[SELECTED_POWERPLANT] to 'text' of typeLabelDetail

        bindAttribute(clientDolphin[STATE][TRIGGER], {
            observableList.clear()
            clientDolphin.data GET, { data ->
                observableList.clear()
                observableList.addAll( data.get(0).get("ids"))
                if (observableList.size()==0){table.setPlaceholder(noData)}
                else{table.setPlaceholder(loading)}

            }
        })
    }

    private void setAnimationFilters(){
        // Initially hiding the Top Pane
        clipRect = new Rectangle();
        clipRect.setWidth(30);
        clipRect.setHeight(boxBounds.getHeight());
        clipRect.translateXProperty().set(boxBounds.getWidth());
        filterStack.setClip(clipRect);
        filterStack.translateXProperty().set(-boxBounds.getWidth());

        // Animation for bouncing effect.
        final Timeline timelineBounce = new Timeline();
        timelineBounce.setCycleCount(2);
        timelineBounce.setAutoReverse(true);
        final KeyValue kv1 = new KeyValue(clipRect.widthProperty(), (boxBounds.getWidth()-15));
        final KeyValue kv2 = new KeyValue(clipRect.translateXProperty(), 15);
        final KeyValue kv3 = new KeyValue(filterStack.translateXProperty(), -15);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(100), kv1, kv2, kv3);
        timelineBounce.getKeyFrames().add(kf1);

        // Event handler to call bouncing effect after the scroll down is finished.
        EventHandler<ActionEvent> onFinished = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                timelineBounce.play();
            }
        };

        timelineRightFilters = new Timeline();
        timelineLeftFilters = new Timeline();

        // Animation for scroll down.
        timelineRightFilters.setCycleCount(1);
        timelineRightFilters.setAutoReverse(true);
        final KeyValue kvDwn1 = new KeyValue(clipRect.widthProperty(), boxBounds.getWidth());
        final KeyValue kvDwn2 = new KeyValue(clipRect.translateXProperty(), 0);
        final KeyValue kvDwn3 = new KeyValue(filterStack.translateXProperty(), 0);
        final KeyFrame kfDwn = new KeyFrame(Duration.millis(200), onFinished, kvDwn1, kvDwn2, kvDwn3);
        timelineRightFilters.getKeyFrames().add(kfDwn);

        // Animation for scroll up.
        timelineLeftFilters.setCycleCount(1);
        timelineLeftFilters.setAutoReverse(true);
        final KeyValue kvUp1 = new KeyValue(clipRect.widthProperty(), 0);
        final KeyValue kvUp2 = new KeyValue(clipRect.translateXProperty(), boxBounds.getWidth());
        final KeyValue kvUp3 = new KeyValue(filterStack.translateXProperty(), -boxBounds.getWidth());
        final KeyFrame kfUp = new KeyFrame(Duration.millis(200), kvUp1, kvUp2, kvUp3);
        timelineLeftFilters.getKeyFrames().add(kfUp);
    }

    public static void bindAttribute(Attribute attribute, Closure closure) {
        final listener = closure as PropertyChangeListener
        attribute.addPropertyChangeListener('value', listener)
    }

    static private void ChoiceBoxListener(CheckBox cb, TableView table, TableColumn col){
        cb.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    table.getColumns().add(col)
                }
                else table.getColumns().remove(col)
            }
        });
    }

}
