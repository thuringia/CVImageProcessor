#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <iostream>
#include <QFileDialog>
#include <QFile>
#include <QMessageBox>
#include <QStandardItemModel>

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::on_actionOpen_triggered()
{
    fileName = QFileDialog::getOpenFileName(this, tr("Open File"), QString(), tr("PGM Files (*.pgm)"));

    if (!fileName.isEmpty()) {
        QFile file(fileName);
        if (!file.open(QIODevice::ReadOnly)) {
            QMessageBox::critical(this, tr("Error"), tr("Could not open file"));
            return;
        }

        pgm_image.load(fileName);

        if(pgm_image.isNull())
        {
            QMessageBox::critical(this, tr("Error"), tr("Could not load image"));
        }

        std::cout << "width: " << pgm_image.width() <<"\nheight: " << pgm_image.height() << std::endl;
        file.close();
    }
    drawImage();
    showMetadata();
}

void MainWindow::on_actionSave_triggered()
{

}

void MainWindow::on_actionQuit_triggered()
{

}

void MainWindow::on_actionInvert_triggered()
{
    pgm_image.invertPixels();
    ui->imageLabel->setPixmap(QPixmap::fromImage(pgm_image));
    ui->centralWidget->resize(ui->gridLayoutWidget->size());
}

void MainWindow::drawImage()
{
    /*QGraphicsPixmapItem pixmap_item(QPixmap::fromImage(pgm_image));
    scene = new QGraphicsScene();
    scene->addItem(&pixmap_item);
    ui->graphicsView->setScene(scene);
    ui->graphicsView->show();
    ui->centralWidget->adjustSize()*/;

    ui->imageLabel->setPixmap(QPixmap::fromImage(pgm_image));
    ui->centralWidget->resize(ui->gridLayoutWidget->size());
}

void MainWindow::showMetadata()
{
    QStringList m_TableHeader;
    m_TableHeader<<"Property"<<"Value";
    int width = pgm_image.width();
    int height = pgm_image.height();
    ui->metadataTableWidget->setHorizontalHeaderLabels(m_TableHeader);
    ui->metadataTableWidget->setItem(0,0, new QTableWidgetItem("File Name:"));
    ui->metadataTableWidget->setItem(1,0, new QTableWidgetItem("Width:"));
    ui->metadataTableWidget->setItem(2,0, new QTableWidgetItem("Height:"));
    ui->metadataTableWidget->setItem(0,1, new QTableWidgetItem(fileName));
    ui->metadataTableWidget->setItem(1,1, new QTableWidgetItem(width));
    ui->metadataTableWidget->setItem(2,1, new QTableWidgetItem(height));

}
