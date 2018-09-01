USE [Matha]
GO

/****** Object:  Table [dbo].[SalesDet]    Script Date: 25-Aug-18 11:35:35 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[SalesDet](
	[SalesDetId] [int] IDENTITY(1,1) NOT NULL,
	[SerialId] [nvarchar](15) NULL,
	[BookId] [int] NULL,
	[BookName] [nvarchar](120) NULL,
	[Qty] [decimal](12, 2) NULL,
	[Rate] [decimal](12, 2) NULL,
	[SlNo] [int] NULL,
	[OrderItemId] [int] NULL,
	CONSTRAINT [SalesDet_PK] PRIMARY KEY CLUSTERED 
	(
		[SalesDetId] ASC
	)
	WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[SalesDet]  WITH CHECK ADD  CONSTRAINT [FK_SalesDet_Sales] FOREIGN KEY([SerialId])
REFERENCES [dbo].[Sales] ([SerialId])
GO

ALTER TABLE [dbo].[SalesDet] CHECK CONSTRAINT [FK_SalesDet_Sales]
GO

ALTER TABLE [dbo].[SalesDet]  WITH CHECK ADD  CONSTRAINT [FK_SalesDet_Book] FOREIGN KEY([BookId])
REFERENCES [dbo].[Book] ([BookId])
GO

ALTER TABLE [dbo].[SalesDet] CHECK CONSTRAINT [FK_SalesDet_Book]
GO

ALTER TABLE [dbo].[SalesDet]  WITH CHECK ADD  CONSTRAINT [FK_SalesDet_SOrderDet] FOREIGN KEY([OrderItemId])
REFERENCES [dbo].[SOrderDet] ([OrderDetId])
GO

ALTER TABLE [dbo].[SalesDet] CHECK CONSTRAINT [FK_SalesDet_SOrderDet]
GO
