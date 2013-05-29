#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QGraphicsPixmapItem>
#include <QGraphicsScene>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT
    
public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();
    void drawImage();
    void showMetadata();
    
private slots:
    void on_actionOpen_triggered();

    void on_actionSave_triggered();

    void on_actionQuit_triggered();

    void on_actionInvert_triggered();

private:
    Ui::MainWindow *ui;
    QImage pgm_image;
    QGraphicsScene* scene;
    QString fileName;
};

#endif // MAINWINDOW_H
